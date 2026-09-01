/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static com.android.launcher3.Utilities.postAsyncCallback;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;
import static com.android.launcher3.util.window.RefreshRateTracker.getSingleFrameMs;
import static com.android.systemui.shared.recents.utilities.Utilities.postAtFrontOfQueueAsynchronously;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.RemoteAnimationTarget;

import androidx.annotation.BinderThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.android.quickstep.HandoffTrace;
import com.android.systemui.animation.RemoteAnimationDelegate;
import com.android.systemui.animation.RemoteAnimationRunnerCompat;

import java.lang.ref.WeakReference;

/**
 * This class is needed to wrap any animation runner that is a part of the
 * RemoteAnimationDefinition:
 * - Launcher creates a new instance of the LauncherAppTransitionManagerImpl whenever it is
 *   created, which in turn registers a new definition
 * - When the definition is registered, window manager retains a strong binder reference to the
 *   runner passed in
 * - If the Launcher activity is recreated, the new definition registered will replace the old
 *   reference in the system's activity record, but until the system server is GC'd, the binder
 *   reference will still exist, which references the runner in the Launcher process, which
 *   references the (old) Launcher activity through this class
 *
 * Instead we make the runner provided to the definition static only holding a weak reference to
 * the runner implementation.  When this animation manager is destroyed, we remove the Launcher
 * reference to the runner, leaving only the weak ref from the runner.
 */
public class LauncherAnimationRunner extends RemoteAnimationRunnerCompat {

    private static final RemoteAnimationFactory DEFAULT_FACTORY =
            (transit, appTargets, wallpaperTargets, nonAppTargets, result) ->
                    result.setAnimation(null, null);

    private final Handler mHandler;
    private final boolean mStartAtFrontOfQueue;
    private final WeakReference<RemoteAnimationFactory> mFactory;

    private AnimationResult mAnimationResult;

    // trebufork: monotonically increasing key so each AnimationResult instance can be traced
    // through finish/cancel in logcat (finish() of an older instance during a new launch would
    // otherwise be indistinguishable from the current open spring's).
    private static int sResultCounter = 0;

    /**
     * @param startAtFrontOfQueue If true, the animation start will be posted at the front of the
     *                            queue to minimize latency.
     */
    public LauncherAnimationRunner(Handler handler, RemoteAnimationFactory factory,
            boolean startAtFrontOfQueue) {
        mHandler = handler;
        mFactory = new WeakReference<>(factory);
        mStartAtFrontOfQueue = startAtFrontOfQueue;
    }

    // Called only in S+ platform
    @BinderThread
    public void onAnimationStart(
            int transit,
            RemoteAnimationTarget[] appTargets,
            RemoteAnimationTarget[] wallpaperTargets,
            RemoteAnimationTarget[] nonAppTargets,
            Runnable runnable) {
        Runnable r = () -> {
            finishExistingAnimation();
            mAnimationResult = new AnimationResult(() -> mAnimationResult = null, runnable);
            HandoffTrace.log("onAnimationStart: new AnimationResult #"
                    + mAnimationResult.mTraceId + " (open launch begins)");
            getFactory().onAnimationStart(transit, appTargets, wallpaperTargets, nonAppTargets,
                    mAnimationResult);
        };
        if (mStartAtFrontOfQueue) {
            postAtFrontOfQueueAsynchronously(mHandler, r);
        } else {
            postAsyncCallback(mHandler, r);
        }
    }

    private RemoteAnimationFactory getFactory() {
        RemoteAnimationFactory factory = mFactory.get();
        return factory != null ? factory : DEFAULT_FACTORY;
    }

    @UiThread
    private void finishExistingAnimation() {
        // A brand-new launch supersedes any pending deferred close (its handoff/token are stale).
        cancelDeferredClose();
        if (mAnimationResult != null) {
            HandoffTrace.log("finishExistingAnimation(#" + mAnimationResult.mTraceId + ")");
            mAnimationResult.finish();
            mAnimationResult = null;
        }
    }

    /**
     * Called by the system
     */    @BinderThread
    @Override
    public void onAnimationCancelled() {
        postAsyncCallback(mHandler, () -> {
            HandoffTrace.log("onAnimationCancelled dispatched");
            // trebufork: if a close handoff is armed (user swiping home during the open
            // animation), WM is about to finish the open transition here and reset the real task
            // surface to identity (the reported full-screen frame). Hide it synchronously BEFORE
            // finish() so the window stays invisible until the close spring takes over rather than
            // flashing full screen for a frame. No-op when no handoff is armed.
            QuickstepTransitionManager.hideHandoffSurfaceOnCancel();
            finishExistingAnimation();
            getFactory().onAnimationCancelled();
        });

    }


    /**
     * Cancels the currently running animation so it freezes at its current position.
     * The launch-merge guard calls this when the user starts closing the app while the
     * open animation is still running, so the close spring starts from exactly where the
     * window is instead of the window continuing to grow to full screen.
     */
    public void cancelCurrentAnimation() {
        postAtFrontOfQueueAsynchronously(mHandler, () -> {
            if (mAnimationResult != null) {
                mAnimationResult.cancelAnimator();
            }
        });
    }

    /**
     * trebufork: reports whether the launch (open) animation runner currently has an active
     * animation result. Used by the launch-merge guard to log, at merge time, whether the open
     * spring is still alive before the close handoff takes over.
     */
    public boolean hasActiveAnimation() {
        return mAnimationResult != null;
    }

    private static Runnable sDeferredCloseRunnable;

    /**
     * trebufork: holds a close gesture that arrived while the open animation was still running,
     * and runs it once the open animation has fully finished (either naturally or cancelled).
     * There is at most one open launch at a time, so a single slot is sufficient.
     *
     * @return true if the callback was armed (an open animation is active and will invoke it),
     *         false if there is no active open animation, in which case the caller may close now.
     */
    public boolean deferCloseUntilAnimationFinished(final Runnable r) {
        if (mAnimationResult == null) {
            return false;
        }
        sDeferredCloseRunnable = r;
        HandoffTrace.log("deferCloseUntilAnimationFinished: armed, will run when open finishes");
        return true;
    }

    /**
     * Dispatches a deferred close (if any) right after the current open animation completes. Runs
     * on the main thread (AnimationResult#finish is main-threaded).
     */
    private static void dispatchDeferredClose() {
        if (sDeferredCloseRunnable != null) {
            Runnable r = sDeferredCloseRunnable;
            sDeferredCloseRunnable = null;
            HandoffTrace.log("dispatchDeferredClose: open finished -> running deferred close");
            try {
                r.run();
            } catch (Throwable e) {
                HandoffTrace.log("dispatchDeferredClose: callback threw "
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private static void cancelDeferredClose() {
        if (sDeferredCloseRunnable != null) {
            HandoffTrace.log("cancelDeferredClose: a new launch superseded the deferred close");
            sDeferredCloseRunnable = null;
            QuickstepTransitionManager.LaunchHandoff.mDeferredClose = false;
        }
    }

    /**
     * trebufork: immediately stops the running open animation at its current position, on this
     * thread, without waiting for the handler queue. Used at the close handoff so the launch
     * animation cannot keep growing the window to full screen during the frames where the close
     * spring takes over - which would otherwise make the close spring visibly "start from a
     * full-screen window" whose on-screen duration matches the (now-frozen) open animation.
     */
    public void cancelCurrentAnimationSync() {
        HandoffTrace.log("cancelCurrentAnimationSync: mAnimationResult="
                + (mAnimationResult != null ? "present" : "NULL"));
        if (mAnimationResult != null) {
            mAnimationResult.cancelAnimator();
        }
    }

    /**
     * Used by RemoteAnimationFactory implementations to run the actual animation and its lifecycle
     * callbacks.
     */
    public static final class AnimationResult extends IRemoteAnimationFinishedCallback.Stub {

        private final Runnable mSyncFinishRunnable;
        private final Runnable mASyncFinishRunnable;

        private AnimatorSet mAnimator;
        private Runnable mOnCompleteCallback;
        private boolean mFinished = false;
        private boolean mInitialized = false;
        final int mTraceId = ++sResultCounter;

        private AnimationResult(Runnable syncFinishRunnable, Runnable asyncFinishRunnable) {
            mSyncFinishRunnable = syncFinishRunnable;
            mASyncFinishRunnable = asyncFinishRunnable;
        }

        /**
         * Cancels the animator, stopping it at its current values. The end listener still fires
         * and finishes the transition (which is what unblocks the close transition), but the
         * window no longer animates towards full screen.
         */
        @UiThread
        private void cancelAnimator() {
            if (mAnimator != null && mAnimator.isStarted() && !mFinished) {
                HandoffTrace.log("cancelAnimator: playTime="
                        + mAnimator.getCurrentPlayTime());
                mAnimator.cancel();
            }
        }

        @UiThread
        private void finish() {
            if (!mFinished) {
                mSyncFinishRunnable.run();
                UI_HELPER_EXECUTOR.execute(() -> {
                    mASyncFinishRunnable.run();
                    if (mOnCompleteCallback != null) {
                        MAIN_EXECUTOR.execute(mOnCompleteCallback);
                    }
                });
                mFinished = true;
                HandoffTrace.log("AnimationResult #" + mTraceId
                        + " finished (open spring done, WM unblocks close)");
                // trebufork: the open animation is now fully done - if a close gesture was
                // deferred (swipe during open), this is the safe point to delegate it as a normal
                // close-from-fullscreen (no full-screen real-surface flash).
                dispatchDeferredClose();
            }
        }

        @UiThread
        public void setAnimation(AnimatorSet animation, Context context) {
            setAnimation(animation, context, null, true);
        }

        /**
         * Sets the animation to play for this app launch
         * @param skipFirstFrame Iff true, we skip the first frame of the animation.
         *                       We set to false when skipping first frame causes jank.
         */
        @UiThread
        public void setAnimation(AnimatorSet animation, Context context,
                @Nullable Runnable onCompleteCallback, boolean skipFirstFrame) {
            if (mInitialized) {
                throw new IllegalStateException("Animation already initialized");
            }
            mInitialized = true;
            // trebufork: master launcher animation speed - scale this launch animation's durations
            // (propotionally per child) by the chosen speed. No-op at 1x or when the developer
            // options animation scales are changed (the system override then takes precedence).
            AnimationSpeed.applyDurationScale(context, animation);
            mAnimator = animation;
            mOnCompleteCallback = onCompleteCallback;
            if (mAnimator == null) {
                finish();
            } else if (mFinished) {
                // Animation callback was already finished, skip the animation.
                mAnimator.start();
                mAnimator.end();
                if (mOnCompleteCallback != null) {
                    mOnCompleteCallback.run();
                }
            } else {
                // Start the animation
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                    }
                });
                if (skipFirstFrame) {
                    // Because t=0 has the app icon in its original spot, we can skip the
                    // first frame and have the same movement one frame earlier.
                    Log.d("b/311077782", "LauncherAnimationRunner.setAnimation");
                    mAnimator.setCurrentPlayTime(
                            Math.min(getSingleFrameMs(context), mAnimator.getTotalDuration()));
                }
                mAnimator.start();
            }
        }

        /**
         * When used as a simple IRemoteAnimationFinishedCallback, this method is used to run the
         * animation finished runnable.
         */
        @Override
        public void onAnimationFinished() {
            HandoffTrace.log("AnimationResult #" + mTraceId
                    + " onAnimationFinished (binder callback from WM)");
            mASyncFinishRunnable.run();
        }
    }

    /**
     * Used with LauncherAnimationRunner as an interface for the runner to call back to the
     * implementation.
     */
    public interface RemoteAnimationFactory extends RemoteAnimationDelegate<AnimationResult> {

        /**
         * Called on the UI thread when the animation targets are received. The implementation must
         * call {@link AnimationResult#setAnimation} with the target animation to be run.
         */
        @Override
        @UiThread
        void onAnimationStart(int transit,
                RemoteAnimationTarget[] appTargets,
                RemoteAnimationTarget[] wallpaperTargets,
                RemoteAnimationTarget[] nonAppTargets,
                LauncherAnimationRunner.AnimationResult result);

        /**
         * Called when the animation is cancelled. This can happen with or without
         * the create being called.
         */
        @Override
        @UiThread
        default void onAnimationCancelled() {}
    }
}
