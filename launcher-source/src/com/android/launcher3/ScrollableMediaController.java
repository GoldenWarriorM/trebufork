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
    // Trebufork: true while the media row controller is actively listening. Kept so duplicate
    // start()/stop() calls are cheap no-ops.
    private boolean mListening;

    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsChangedListener =
            controllers -> setActiveController(pickController(controllers));

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
        try {
            // The user handle argument is only relevant with a notification listener; the
            // privileged MEDIA_CONTENT_CONTROL permission ignores it (-1 = all users).
            mSessionManager.addOnActiveSessionsChangedListener(
                    mSessionsChangedListener, new ComponentName(mContext, "none"), mMainHandler);
            List<MediaController> controllers =
                    mSessionManager.getActiveSessions(new ComponentName(mContext, "none"));
            setActiveController(pickController(controllers));
        } catch (SecurityException e) {
            // Privileged permission missing (e.g. debug install outside the Magisk module):
            // the row stays empty rather than crashing the launcher.
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
        setActiveController(null);
    }

    /** The active media controller, or null when nothing is playing. */
    @Nullable
    public MediaController getController() {
        return mController;
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

    @Nullable
    private MediaController pickController(@Nullable List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) {
            return null;
        }
        // Prefer the session that is actively playing; fall back to the most recent one.
        MediaController best = null;
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            boolean playing = state != null && (state.getState() == PlaybackState.STATE_PLAYING
                    || state.getState() == PlaybackState.STATE_BUFFERING
                    || state.getState() == PlaybackState.STATE_FAST_FORWARDING
                    || state.getState() == PlaybackState.STATE_REWINDING);
            if (playing) {
                return controller;
            }
            if (best == null || state != null) {
                best = controller;
            }
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
