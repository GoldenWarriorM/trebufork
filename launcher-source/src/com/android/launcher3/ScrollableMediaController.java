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
    // Per-callback owner map: MediaController.Callback has no session reference, so
    // observeSessions remembers which controller each listener instance is attached to.
    private final java.util.Map<MediaController.Callback, MediaController> mObserverOwners =
            new java.util.HashMap<>();
    // The session that most recently entered STATE_PLAYING (weakly held): the preferred
    // winner when several sessions still report "playing".
    @Nullable
    private java.lang.ref.WeakReference<MediaController> mLastPlayedController;
    // Trebufork: true while the media row controller is actively listening. Kept so duplicate
    // start()/stop() calls are cheap no-ops.
    private boolean mListening;

    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsChangedListener =
            controllers -> {
                int oldSize = mSessions.size();
                observeSessions(filterPhantomSessions(controllers));
                clearPinIfDead();
                setActiveController(pickController(mSessions));
                if (mSessions.size() != oldSize) {
                    notifySessionListChanged();
                }
            };

    /**
     * trebufork: the shade only shows players backed by a live media notification
     * (MediaDataManager builds media data from notifications). Some apps (Boosty, some
     * browsers) register a MediaSession without ever posting one — those sessions have no
     * artwork and dead buttons, so they are dropped here. The gate is only applied when the
     * launcher's notification listener has data; if the listener is not connected (e.g. right
     * after boot), sessions are kept so a real player is never lost — its notification (and
     * small icon) will arrive and nothing needs to be re-filtered.
     */
    private List<MediaController> filterPhantomSessions(
            @Nullable List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()
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
                        // whichever session this listener instance is attached to.
                        MediaController owner = mObserverOwners.get(this);
                        if (owner != null) {
                            mLastPlayedController = new java.lang.ref.WeakReference<>(owner);
                        }
                    }
                    setActiveController(pickController(mSessions));
                }
            };

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
        }
        mSessions = controllers == null
                ? java.util.Collections.emptyList() : new java.util.ArrayList<>(controllers);
        for (MediaController controller : mSessions) {
            MediaController.Callback callback = new SessionObserver();
            mObserverOwners.put(callback, controller);
            try {
                controller.registerCallback(callback, mMainHandler);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    /** Per-session playback observer; the owner is tracked in {@link #mObserverOwners}. */
    private class SessionObserver extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                MediaController owner = mObserverOwners.get(this);
                if (owner != null) {
                    mLastPlayedController = new java.lang.ref.WeakReference<>(owner);
                }
            }
            setActiveController(pickController(mSessions));
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
        return new java.util.ArrayList<>(mSessions);
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
        mPinnedToken = controller == null ? null : controller.getSessionToken();
        setActiveController(controller);
    }

    /**
     * trebufork: drops the user's swipe pin (if any) and re-picks the active session by the
     * normal rules. The launcher media row calls this when its window loses focus: while
     * home is in the background the carousel must behave like the shade — the most recently
     * playing session wins, a stale pin must not hold the active player on an old session
     * while a NEW one starts playing in another app.
     */
    public void clearPin() {
        if (mPinnedToken == null) {
            return;
        }
        mPinnedToken = null;
        setActiveController(pickController(mSessions));
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
