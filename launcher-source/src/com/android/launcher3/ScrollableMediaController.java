/*
 * Copyright (C) 2026 The trebufork Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * trebufork: watches the active media session for the scrollable-home media row, the same way
 * the SystemUI shade / lock screen media controls do. Uses the privileged
 * {@code MEDIA_CONTENT_CONTROL} permission (whitelisted for the platform-signed launcher) so
 * no notification listener is required.
 *
 * <p>Everything is delivered on the main thread. Listeners see the current controller whenever
 * it changes (session starts / ends / switches apps); metadata and playback-state changes are
 * surfaced through a single {@link Listener#onMediaChanged()} callback.
 */
public class ScrollableMediaController {

    /** Observers of the active media session state. Called on the main thread. */
    public interface Listener {
        /** The active session changed (started, ended, or switched to another app). */
        default void onActiveControllerChanged() {}

        /** Metadata or playback state of the active session changed. */
        default void onMediaChanged() {}

        /**
         * trebufork: the set of active sessions changed (a session appeared, died, or was
         * re-filtered) — the carousel reorders while the launcher window is not active, so
         * it needs to rebuild even when the ACTIVE session did not change.
         */
        default void onSessionListChanged() {}
    }

    private final Context mContext;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private final MediaSessionManager mSessionManager;
    private final List<Listener> mListeners = new java.util.ArrayList<>();

    @Nullable
    private MediaController mController;
    @Nullable
    private String mAppName;
    @Nullable
    private Drawable mAppIcon;
    // All currently active sessions. Every one of them is observed, so a playback-state
    // change in a background player (e.g. the user hits play in YouTube while Metrolist's
    // session is still registered) re-ranks the picker — like SystemUI, which re-ranks on
    // every media update instead of only when the session set changes.
    private List<MediaController> mSessions = java.util.Collections.emptyList();
    // The session the user pinned by swiping the carousel to it; preferred over the
    // auto-pick while its session stays alive (SystemUI keeps the swiped-to page visible).
    @Nullable
    private MediaSession.Token mPinnedToken;
    // True when the pin came from a carousel swipe (the user explicitly chose the player):
    // a swipe-pinned session must NOT be reordered to the front of the carousel — the user
    // is already looking at it, moving its page would replay an animation over their own
    // gesture. Only an organically changed active session (a new app starts playing)
    // triggers the move-to-front reorder.
    private boolean mPinnedBySwipe;
    // Per-callback owner map: MediaController.Callback has no session reference, so
    // observeSessions remembers which controller each listener instance is attached to.
    private final java.util.Map<MediaController.Callback, MediaController> mObserverOwners =
            new java.util.HashMap<>();
    // The session that most recently entered STATE_PLAYING (weakly held): the preferred
    // winner when several sessions still report "playing".
    @Nullable
    private java.lang.ref.WeakReference<MediaController> mLastPlayedController;
    // Last playback state seen per session token: distinguishes a real transition into
    // STATE_PLAYING (a takeover) from apps re-posting the same (stagnated) state, which
    // must never re-run the pick.
    private final java.util.Map<MediaSession.Token, PlaybackState> mLastKnownStates =
            new java.util.HashMap<>();
    // ------------------------------------------------------------------
    // trebufork port of SystemUI's MediaTimeoutListener (media/controls/domain/pipeline/):
    // a session paused for PAUSED_MEDIA_TIMEOUT (10 min, like debug.sysui.media_timeout)
    // "times out" — it leaves the carousel, exactly like a player expiring from the
    // shade. Playing again or a playback-state change restarts the timeout.
    // ------------------------------------------------------------------
    private static final long PAUSED_MEDIA_TIMEOUT_MS = 10 * 60 * 1000L;
    // Pending timeout runnables per session token (MediaTimeoutListener.cancellation).
    private final java.util.Map<MediaSession.Token, Runnable> mTimeouts = new java.util.HashMap<>();
    // Sessions that have already timed out; filtered from the carousel until they play.
    private final java.util.Set<MediaSession.Token> mTimedOut = new java.util.HashSet<>();
    // Trebufork: true while the media row controller is actively listening. Kept so duplicate
    // start()/stop() calls are cheap no-ops.
    private boolean mListening;
    // Trebufork: the notification-driven phantom filter is DEBOUNCED. Media notifications
    // flicker (Bluetooth posts a media-style notification in ERROR state and drops it on
    // AVRCP updates; players re-post on metadata changes) — applying the gate instantly in
    // both directions made the carousel player count flap between 2 and 3. SystemUI masks
    // the same churn with VisualStabilityProvider (reordering suppressed during
    // notification transactions); here a settle delay merges the burst into a single
    // re-filter, and a flicker shorter than the delay never changes the carousel.
    private static final long MEDIA_FILTER_SETTLE_MS = 3000L;
    private final Runnable mFilterSettle = this::applyRefilterNow;

    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsChangedListener =
            controllers -> {
                int oldSize = mSessions.size();
                StringBuilder pkgs = new StringBuilder();
                if (controllers != null) {
                    for (MediaController c : controllers) {
                        pkgs.append(c.getPackageName()).append(' ');
                    }
                }
                android.util.Log.d("TrebuforkMedia", "onSessionsChanged old=" + oldSize
                        + " new=[" + pkgs.toString().trim() + "]");
                observeSessions(holdVanishingSessions(filterPhantomSessions(controllers)));
                clearPinIfDead();
                setActiveController(pickController(mSessions));
                if (mSessions.size() != oldSize) {
                    notifySessionListChanged();
                }
            };

    // Trebufork: the raw MediaSessionManager list FLICKERS when apps re-register their
    // sessions (opening recents / another app briefly drops a session and re-adds it a
    // moment later — measured: [mpvex, metrolist] → [metrolist] → back within a second).
    // Tracking that flicker made the carousel player count flap. A package that vanishes
    // from the list is HELD in the visible set for SESSION_VANISH_GRACE_MS and dropped
    // only if it is still absent when the grace expires (a real player close).
    private static final long SESSION_VANISH_GRACE_MS = 5000L;
    private final java.util.Map<String, Runnable> mVanishGrace = new java.util.HashMap<>();

    /** Keeps recently-vanished packages for the grace window (see {@link #mVanishGrace}). */
    private List<MediaController> holdVanishingSessions(List<MediaController> filtered) {
        java.util.Set<String> present = new java.util.HashSet<>();
        if (filtered != null) {
            for (MediaController c : filtered) {
                present.add(c.getPackageName());
            }
        }
        // Cancel grace for packages that are back.
        for (String pkg : new java.util.HashSet<>(mVanishGrace.keySet())) {
            if (present.contains(pkg)) {
                Runnable r = mVanishGrace.remove(pkg);
                if (r != null) {
                    mMainHandler.removeCallbacks(r);
                }
            }
        }
        if (filtered == null || !mListening) {
            return filtered;
        }
        List<MediaController> result = new java.util.ArrayList<>(filtered);
        for (MediaController kept : mSessions) {
            String pkg = kept.getPackageName();
            if (!present.contains(pkg) && !mVanishGrace.containsKey(pkg)) {
                Runnable grace = new Runnable() {
                    @Override
                    public void run() {
                        mVanishGrace.remove(pkg);
                        applyRefilterNow();
                    }
                };
                mVanishGrace.put(pkg, grace);
                mMainHandler.postDelayed(grace, SESSION_VANISH_GRACE_MS);
                android.util.Log.d("TrebuforkMedia",
                        "holding vanished session for grace: " + pkg);
                result.add(kept);
            } else if (!present.contains(pkg)) {
                // Already in its grace window: keep holding it.
                result.add(kept);
            }
        }
        return result;
    }

    /**
     * trebufork: the shade only shows players backed by a live media notification
     * (MediaDataManager builds media data from notifications). Some apps (Boosty, some
     * browsers) register a MediaSession without ever posting one — those sessions have no
     * artwork and dead buttons, so they are dropped here. The gate is only applied when the
     * launcher's notification listener has data; if the listener is not connected (e.g. right
     * after boot), sessions are kept so a real player is never lost — its notification (and
     * small icon) will arrive and nothing needs to be re-filtered.
     */
    /**
     * Trebufork: some apps (mpv-based players, some games) register several MediaSessions
     * for a single playback — the carousel must not show the same player twice. One
     * controller per package is kept. The pick is STICKY: the previously kept session
     * survives while it is still in the live list, even when the system list order flips
     * (mpv alternates its two sessions 'mpv'/'MediaPlaybackService' — a non-sticky pick
     * made the representative token alternate, so the card was dropped and re-bound on
     * every sessions-changed, replaying the bind flash and flipping the anchored page).
     * Only a NEWLY PLAYING duplicate takes over from a non-playing kept one.
     */
    private List<MediaController> dedupeByPackage(List<MediaController> controllers) {
        java.util.Map<String, MediaController> previousByPackage = new java.util.HashMap<>();
        for (MediaController c : mSessions) {
            previousByPackage.putIfAbsent(c.getPackageName(), c);
        }
        List<MediaController> result = new java.util.ArrayList<>(controllers.size());
        java.util.Map<String, MediaController> byPackage = new java.util.HashMap<>();
        for (MediaController controller : controllers) {
            String pkg = controller.getPackageName();
            MediaController kept = byPackage.get(pkg);
            if (kept == null) {
                byPackage.put(pkg, controller);
                result.add(controller);
            } else {
                PlaybackState keptState = kept.getPlaybackState();
                PlaybackState newState = controller.getPlaybackState();
                boolean keptPlaying = keptState != null
                        && keptState.getState() == PlaybackState.STATE_PLAYING;
                boolean newPlaying = newState != null
                        && newState.getState() == PlaybackState.STATE_PLAYING;
                MediaController previous = previousByPackage.get(pkg);
                boolean previousAlive = previous != null
                        && controller.getSessionToken().equals(previous.getSessionToken());
                if (!keptPlaying && newPlaying
                        && !(previousAlive && !isPlayingState(previous))) {
                    // A duplicate just started playing while the kept one did not — take
                    // it over UNLESS the previous representative is still alive and not
                    // playing (mpv's second session reports PLAYING while the first is
                    // paused: switching would bounce the card between two sessions).
                    result.set(result.indexOf(kept), controller);
                    byPackage.put(pkg, controller);
                }
                android.util.Log.d("TrebuforkMedia",
                        "dropping duplicate session for package: " + pkg);
            }
        }
        return result;
    }

    private static boolean isPlayingState(MediaController controller) {
        PlaybackState state = controller == null ? null : controller.getPlaybackState();
        return state != null && state.getState() == PlaybackState.STATE_PLAYING;
    }

    private List<MediaController> filterPhantomSessions(
            @Nullable List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) {
            return controllers;
        }
        // Drop dead sessions up front: the Bluetooth stack keeps a persistent session in
        // PlaybackState.STATE_ERROR ("Bluetooth audio disconnected") that has no metadata,
        // no artwork and no notification — the shade never shows it (its MediaData comes
        // from notifications), while the raw session list does.
        java.util.List<MediaController> live = new java.util.ArrayList<>(controllers.size());
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            if (state != null && state.getState() == PlaybackState.STATE_ERROR) {
                android.util.Log.d("TrebuforkMedia",
                        "dropping error-state session: " + controller.getPackageName());
                continue;
            }
            live.add(controller);
        }
        controllers = live;
        controllers = dedupeByPackage(controllers);
        if (controllers.isEmpty()
                || !com.android.launcher3.notification.NotificationListener
                        .isListenerPopulated()) {
            return controllers;
        }
        List<MediaController> result = new java.util.ArrayList<>(controllers.size());
        for (MediaController controller : controllers) {
            if (com.android.launcher3.notification.NotificationListener.hasMediaNotification(
                    controller.getPackageName())) {
                result.add(controller);
            } else {
                android.util.Log.d("TrebuforkMedia",
                        "dropping phantom session (no media notification): "
                                + controller.getPackageName());
            }
        }
        return result;
    }

    // Registered on EVERY active session: any playback-state change anywhere re-runs the
    // pick (a no-op if the winner didn't change). A session that just entered STATE_PLAYING
    // is remembered as the user's latest choice, so it wins over other sessions that
    // stagnate in STATE_PLAYING (some apps never leave that state when paused).
    private final MediaController.Callback mSessionObserverCallback =
            new MediaController.Callback() {
                @Override
                public void onPlaybackStateChanged(@Nullable PlaybackState state) {
                    if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                        // The callback is registered per-controller, so its owner is
                        // whichever session this listener instance is attached to. Only a
                        // session that is the package's CURRENT representative may claim
                        // the last-played slot: apps with two sessions (mpv) report PLAYING
                        // from the duplicate too, and honoring it flipped the pick between
                        // the two representatives on every report.
                        MediaController owner = mObserverOwners.get(this);
                        if (owner != null && isPackageRepresentative(owner)) {
                            mLastPlayedController = new java.lang.ref.WeakReference<>(owner);
                        }
                    }
                    setActiveController(pickController(mSessions));
                }
            };

    /** True when the controller is the kept (deduped) session of its package. */
    private boolean isPackageRepresentative(MediaController controller) {
        String pkg = controller.getPackageName();
        for (MediaController session : mSessions) {
            if (pkg.equals(session.getPackageName())) {
                // mSessions is already deduped: the FIRST entry per package is the
                // representative.
                return session.getSessionToken().equals(controller.getSessionToken());
            }
        }
        return false;
    }

    private final MediaController.Callback mControllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            notifyMediaChanged();
        }

        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            notifyMediaChanged();
        }

        @Override
        public void onQueueChanged(@Nullable List<MediaSession.QueueItem> queue) {
            notifyMediaChanged();
        }
    };

    public ScrollableMediaController(Context context) {
        mContext = context.getApplicationContext();
        MediaSessionManager manager = null;
        try {
            manager = mContext.getSystemService(MediaSessionManager.class);
        } catch (Throwable t) {
            // Service unavailable on some builds; the row will simply never show content.
        }
        mSessionManager = manager;
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    /** Starts listening for media sessions. Cheap to call repeatedly. */
    public void start() {
        if (mListening || mSessionManager == null) {
            return;
        }
        mListening = true;
        try {
            // The user handle argument is only relevant with a notification listener; the
            // privileged MEDIA_CONTENT_CONTROL permission ignores it (-1 = all users).
            mSessionManager.addOnActiveSessionsChangedListener(
                    mSessionsChangedListener, new ComponentName(mContext, "none"), mMainHandler);
            List<MediaController> controllers =
                    mSessionManager.getActiveSessions(new ComponentName(mContext, "none"));
            observeSessions(filterPhantomSessions(controllers));
            setActiveController(pickController(mSessions));
            // trebufork: the first full-refresh of the notification listener populates the
            // media-notification map — at boot the session set is usually known BEFORE that,
            // so phantom sessions (e.g. Boosty registering a session with no media
            // notification) survived the empty-map skip. Re-filter as soon as the listener
            // has data.
            com.android.launcher3.notification.NotificationListener.addMediaSmallIconListener(
                    mSmallIconListener);
        } catch (SecurityException e) {
            // Privileged permission missing (e.g. debug install outside the Magisk module):
            // the row stays empty rather than crashing the launcher.
            mListening = false;
        }
    }

    /** Stops listening. Cheap to call repeatedly. */
    public void stop() {
        if (!mListening || mSessionManager == null) {
            return;
        }
        mListening = false;
        mMainHandler.removeCallbacks(mFilterSettle);
        for (Runnable grace : mVanishGrace.values()) {
            mMainHandler.removeCallbacks(grace);
        }
        mVanishGrace.clear();
        try {
            mSessionManager.removeOnActiveSessionsChangedListener(mSessionsChangedListener);
        } catch (SecurityException ignored) {
        }
        com.android.launcher3.notification.NotificationListener
                .removeMediaSmallIconListener(mSmallIconListener);
        observeSessions(java.util.Collections.emptyList());
        setActiveController(null);
    }

    // trebufork: fired when the notification listener's media map changes (first populate,
    // notification posted/removed). Re-runs the phantom filter on the CURRENT session set —
    // the boot-time phantom that survived the empty-map skip is dropped the moment the
    // listener reports it has no media notification for its package.
    private final com.android.launcher3.notification.NotificationListener.MediaSmallIconListener
            mSmallIconListener = this::refilterSessions;

    private void refilterSessions() {
        if (!mListening || mSessions.isEmpty()) {
            return;
        }
        // Debounce: media notifications flicker; wait for the burst to settle before
        // reshaping the carousel. A notification that comes back within the window is
        // never treated as gone.
        mMainHandler.removeCallbacks(mFilterSettle);
        mMainHandler.postDelayed(mFilterSettle, MEDIA_FILTER_SETTLE_MS);
    }

    /** The debounced re-filter body: applies the current notification gate once. */
    private void applyRefilterNow() {
        if (!mListening || mSessions.isEmpty()) {
            return;
        }
        List<MediaController> filtered =
                filterPhantomSessions(new java.util.ArrayList<>(mSessions));
        if (filtered.size() != mSessions.size()) {
            observeSessions(filtered);
            clearPinIfDead();
            setActiveController(pickController(mSessions));
            notifyControllerChanged();
            notifySessionListChanged();
        }
    }

    /** Swaps the per-session observer callbacks to the new session set. */
    private void observeSessions(@Nullable List<MediaController> controllers) {
        for (MediaController old : mSessions) {
            MediaController.Callback callback = null;
            for (java.util.Map.Entry<MediaController.Callback, MediaController> entry
                    : mObserverOwners.entrySet()) {
                if (old.equals(entry.getValue())) {
                    callback = entry.getKey();
                    break;
                }
            }
            if (callback != null) {
                mObserverOwners.remove(callback);
                try {
                    old.unregisterCallback(callback);
                } catch (IllegalStateException ignored) {
                }
            }
        }        mSessions = controllers == null
                ? java.util.Collections.emptyList() : new java.util.ArrayList<>(controllers);
        // Forget states of removed sessions and seed the new ones, so the first delivery
        // of an unchanged state is not mistaken for a transition.
        java.util.Set<MediaSession.Token> known = new java.util.HashSet<>();
        for (MediaController controller : mSessions) {
            known.add(controller.getSessionToken());
            mLastKnownStates.putIfAbsent(controller.getSessionToken(),
                    controller.getPlaybackState());
        }
        mLastKnownStates.keySet().retainAll(known);
        // MediaTimeoutListener: drop bookkeeping for removed sessions, seed new ones.
        mTimeouts.keySet().retainAll(known);
        mTimedOut.retainAll(known);
        for (MediaController controller : mSessions) {
            if (!mTimeouts.containsKey(controller.getSessionToken())) {
                armTimeout(controller.getSessionToken());
            }
            MediaController.Callback callback = new SessionObserver();
            mObserverOwners.put(callback, controller);
            try {
                controller.registerCallback(callback, mMainHandler);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    /** Schedules the paused-player timeout for a session (MediaTimeoutListener). */
    private void armTimeout(MediaSession.Token token) {
        cancelTimeout(token);
        Runnable timeout = () -> {
            mTimeouts.remove(token);
            if (mTimedOut.add(token)) {
                android.util.Log.d("TrebuforkMedia",
                        "session paused timeout -> hidden from carousel");
                // The timed-out player leaves the carousel; it comes back the moment it
                // plays again (SessionObserver clears the flag on a PLAYING transition).
                notifySessionListChanged();
                if (mController != null && token.equals(mController.getSessionToken())) {
                    clearPinIfDead();
                    setActiveController(pickController(mSessions));
                    notifyControllerChanged();
                }
            }
        };
        mTimeouts.put(token, timeout);
        mMainHandler.postDelayed(timeout, PAUSED_MEDIA_TIMEOUT_MS);
    }

    private void cancelTimeout(MediaSession.Token token) {
        Runnable timeout = mTimeouts.remove(token);
        if (timeout != null) {
            mMainHandler.removeCallbacks(timeout);
        }
    }

    /** Per-session playback observer; the owner is tracked in {@link #mObserverOwners}. */
    private class SessionObserver extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            MediaController owner = mObserverOwners.get(this);
            if (owner == null) {
                return;
            }
            PlaybackState prev = mLastKnownStates.get(owner.getSessionToken());
            mLastKnownStates.put(owner.getSessionToken(), state);
            // trebufork: the pick is sticky — the carousel is stolen ONLY by a session
            // that genuinely (re)starts playing:
            //  1. a real transition into STATE_PLAYING (the user pressed play there), or
            //  2. a PLAYING report from a NON-active session whose state actually changed
            //     (the previous player's playback does not end the instant the new one
            //     starts, and many apps stay stuck in STATE_PLAYING while paused — a
            //     bare PLAYING re-post of an unchanged state must not flip the pick,
            //     but pressing play in such a stale session must take over at once).
            // Anything else (a pause of the active player, an identical re-post) keeps
            // the current pick.
            boolean takeover = false;
            if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                boolean wasPlaying = prev != null
                        && prev.getState() == PlaybackState.STATE_PLAYING;
                if (!wasPlaying) {
                    takeover = true;
                } else if (!owner.equals(mController) && !samePlayback(prev, state)) {
                    takeover = true;
                }
            }
            if (takeover) {
                mLastPlayedController = new java.lang.ref.WeakReference<>(owner);
            }
            // MediaTimeoutListener semantics: playing cancels the timeout (and un-hides a
            // timed-out player); not playing arms/re-arms the 10-minute expiration.
            if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                if (mTimedOut.remove(owner.getSessionToken())) {
                    android.util.Log.d("TrebuforkMedia",
                            "timed-out session started playing -> back in carousel");
                    notifySessionListChanged();
                }
                cancelTimeout(owner.getSessionToken());
            } else {
                armTimeout(owner.getSessionToken());
            }
            if (takeover) {
                setActiveController(pickController(mSessions));
            }
        }

        /** True when two playback states report the same (re-posted) playback. */
        private boolean samePlayback(PlaybackState a, PlaybackState b) {
            return a.getState() == b.getState()
                    && a.getPosition() == b.getPosition()
                    && a.getLastPositionUpdateTime() == b.getLastPositionUpdateTime();
        }
    }

    /** The active media controller, or null when nothing is playing. */
    @Nullable
    public MediaController getController() {
        return mController;
    }

    /**
     * The carousel sessions in stable system order (NO active-first reordering): the
     * carousel is a real HorizontalScrollView, so the page set must not shuffle while the
     * user is looking at it — the active session is just highlighted by scrolling to its
     * page instead.
     */
    public List<MediaController> getSessionList() {
        List<MediaController> result = new java.util.ArrayList<>(mSessions.size());
        for (MediaController session : mSessions) {
            // MediaTimeoutListener: a player paused for 10 minutes leaves the carousel.
            if (!mTimedOut.contains(session.getSessionToken())) {
                result.add(session);
            }
        }
        return result;
    }

    /**
     * The ordered list of media sessions for the player carousel: the active one first,
     * the rest in system order. Stable across playback-state changes (unlike the pick).
     */
    public List<MediaController> getCarouselSessions() {
        List<MediaController> result = new java.util.ArrayList<>(mSessions);
        if (mController != null) {
            result.remove(mController);
            result.add(0, mController);
        }
        return result;
    }

    /**
     * Pins the carousel to the given session (a swipe): it stays active while alive, even
     * if another session starts playing (the user explicitly chose it).
     */
    public void pinSession(@Nullable MediaController controller) {
        pinSession(controller, false);
    }

    /**
     * Pins the carousel to the given session, remembering whether the pin came from a
     * carousel swipe ({@code fromSwipe}) — see {@link #isPinnedBySwipe()}.
     */
    public void pinSession(@Nullable MediaController controller, boolean fromSwipe) {
        mPinnedToken = controller == null ? null : controller.getSessionToken();
        mPinnedBySwipe = fromSwipe && controller != null;
        setActiveController(controller);
    }

    /**
     * True while the active session is pinned by a carousel swipe: the row must not
     * reorder the pages in response to this activation (the user just swiped to it).
     */
    public boolean isPinnedBySwipe() {
        return mPinnedToken != null && mPinnedBySwipe;
    }

    /**
     * trebufork: drops the user's swipe pin (if any) and re-picks the active session by the
     * normal rules. The launcher media row calls this when its window loses focus: while
     * home is in the background the carousel must behave like the shade — a session that
     * genuinely STARTS playing takes over, but a mere pause of the current player must not
     * shuffle the active pick to another stale "playing" session (the shade's carousel is
     * a TreeMap ordered at insertion; a pause alone never reorders it).
     */
    public void clearPin() {
        if (mPinnedToken == null) {
            // No swipe pin: keep the current active session unless it DIED. A pause must
            // not re-pick here — the sticky takeover rules (a real STATE_PLAYING
            // transition) already handle a new app starting playback while unfocused.
            if (mController != null && !sessionAlive(mController)) {
                setActiveController(pickController(mSessions));
            }
            return;
        }
        mPinnedToken = null;
        mPinnedBySwipe = false;
        if (mController == null || !sessionAlive(mController)) {
            setActiveController(pickController(mSessions));
        }
    }

    /** True when the controller's session is still in the live session set. */
    private boolean sessionAlive(MediaController controller) {
        for (MediaController session : mSessions) {
            if (session.getSessionToken().equals(controller.getSessionToken())) {
                return true;
            }
        }
        return false;
    }

    /** Drops the carousel pin if the pinned session died. */
    private void clearPinIfDead() {
        if (mPinnedToken == null) {
            return;
        }
        for (MediaController controller : mSessions) {
            if (controller.getSessionToken().equals(mPinnedToken)) {
                return;
            }
        }
        mPinnedToken = null;
    }

    /** Human-readable name of the app that owns the active session, or null. */
    @Nullable
    public String getAppName() {
        return mAppName;
    }

    /** Icon of the app that owns the active session, or null. */
    @Nullable
    public Drawable getAppIcon() {
        return mAppIcon;
    }

    /** Current playback position in ms, extrapolated from the last playback state. */
    public long getEstimatedPosition() {
        PlaybackState state = mController == null ? null : mController.getPlaybackState();
        if (state == null) {
            return 0L;
        }
        return state.getPosition();
    }

    /**
     * The pinned carousel session wins over the auto-pick while it is alive (the user
     * swiped to it explicitly).
     */
    @Nullable
    private MediaController pickController(@Nullable List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) {
            return null;
        }
        if (mPinnedToken != null) {
            for (MediaController controller : controllers) {
                if (controller.getSessionToken().equals(mPinnedToken)) {
                    return controller;
                }
            }
        }
        MediaController lastPlayed = mLastPlayedController == null
                ? null : mLastPlayedController.get();
        // The shade's carousel is a TreeMap ordered at INSERTION time: a pause alone
        // never reshuffles it. So the currently active session stays the pick while its
        // session is alive, even after it paused — only a genuinely NEW playing session
        // (a real transition into STATE_PLAYING, which sets mLastPlayedController) or a
        // dead current session may change the pick.
        if (mController != null && lastPlayed != null && lastPlayed.equals(mController)
                && controllers.contains(mController)) {
            return mController;
        }
        // Prefer the session that is actively playing; among several "playing" sessions
        // (apps that stagnate in STATE_PLAYING) the one the user played last wins, matching
        // SystemUI which shows the most recently active media. Fall back to the first
        // session with a known state.
        MediaController best = null;
        MediaController firstPlaying = null;
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            boolean playing = state != null && (state.getState() == PlaybackState.STATE_PLAYING
                    || state.getState() == PlaybackState.STATE_BUFFERING
                    || state.getState() == PlaybackState.STATE_FAST_FORWARDING
                    || state.getState() == PlaybackState.STATE_REWINDING);
            if (playing) {
                if (controller.equals(lastPlayed)) {
                    return controller;
                }
                if (firstPlaying == null) {
                    firstPlaying = controller;
                }
            }
            if (best == null || state != null) {
                best = controller;
            }
        }
        if (firstPlaying != null) {
            return firstPlaying;
        }
        return best;
    }

    private void setActiveController(@Nullable MediaController controller) {
        if (mController == controller
                || (mController != null && mController.equals(controller))) {
            return;
        }
        if (mController != null) {
            try {
                mController.unregisterCallback(mControllerCallback);
            } catch (IllegalStateException ignored) {
            }
        }
        mController = controller;
        mAppName = null;
        mAppIcon = null;
        if (mController != null) {
            try {
                mController.registerCallback(mControllerCallback, mMainHandler);
            } catch (IllegalStateException ignored) {
            }
            resolveOwnerInfo();
        }
        notifyControllerChanged();
        notifyMediaChanged();
    }

    private void resolveOwnerInfo() {
        if (mController == null) {
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        String packageName = mController.getPackageName();
        try {
            android.content.pm.ApplicationInfo info =
                    pm.getApplicationInfo(packageName, 0);
            mAppName = pm.getApplicationLabel(info).toString();
            mAppIcon = pm.getApplicationIcon(info);
        } catch (PackageManager.NameNotFoundException e) {
            mAppName = packageName;
        }
    }

    private void notifySessionListChanged() {
        mMainHandler.post(() -> {
            for (Listener listener : copyListeners()) {
                listener.onSessionListChanged();
            }
        });
    }

    private void notifyControllerChanged() {
        mMainHandler.post(() -> {
            for (Listener listener : copyListeners()) {
                listener.onActiveControllerChanged();
            }
        });
    }

    private void notifyMediaChanged() {
        mMainHandler.post(() -> {
            for (Listener listener : copyListeners()) {
                listener.onMediaChanged();
            }
        });
    }

    private Listener[] copyListeners() {
        return mListeners.toArray(new Listener[0]);
    }
}
