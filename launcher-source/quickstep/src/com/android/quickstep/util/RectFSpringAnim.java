/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.quickstep.util;


import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.android.launcher3.AnimationSpeed;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.DynamicResource;
import com.android.quickstep.RemoteAnimationTargets.ReleaseCheck;
import com.android.systemui.plugins.ResourceProvider;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;


/**
 * Applies spring forces to animate from a starting rect to a target rect,
 * while providing update callbacks to the caller.
 */
public class RectFSpringAnim extends ReleaseCheck {

    // trebufork: shared tag for close-to-icon animation lifecycle diagnostics (stuck floating
    // icon / black wallpaper after rapid open-close). Filter: adb logcat -s TrebuforkAnim
    private static final String TREBUFORK_TAG = "TrebuforkAnim";

    private static final FloatPropertyCompat<RectFSpringAnim> RECT_CENTER_X =
            new FloatPropertyCompat<RectFSpringAnim>("rectCenterXSpring") {
                @Override
                public float getValue(RectFSpringAnim anim) {
                    return anim.mCurrentCenterX;
                }

                @Override
                public void setValue(RectFSpringAnim anim, float currentCenterX) {
                    anim.mCurrentCenterX = currentCenterX;
                    anim.onUpdate();
                }
            };

    private static final FloatPropertyCompat<RectFSpringAnim> RECT_Y =
            new FloatPropertyCompat<RectFSpringAnim>("rectYSpring") {
                @Override
                public float getValue(RectFSpringAnim anim) {
                    return anim.mCurrentY;
                }

                @Override
                public void setValue(RectFSpringAnim anim, float y) {
                    anim.mCurrentY = y;
                    anim.onUpdate();
                }
            };

    private static final FloatPropertyCompat<RectFSpringAnim> RECT_SCALE_PROGRESS =
            new FloatPropertyCompat<RectFSpringAnim>("rectScaleProgress") {
                @Override
                public float getValue(RectFSpringAnim object) {
                    return object.mCurrentScaleProgress;
                }

                @Override
                public void setValue(RectFSpringAnim object, float value) {
                    object.mCurrentScaleProgress = value;
                    object.onUpdate();
                }
            };

    private final RectF mStartRect;
    private final RectF mTargetRect;
    private final RectF mCurrentRect = new RectF();
    private final List<OnUpdateListener> mOnUpdateListeners = new ArrayList<>();
    private final List<Animator.AnimatorListener> mAnimatorListeners = new ArrayList<>();

    private float mCurrentCenterX;
    private float mCurrentY;
    // If true, tracking the bottom of the rects, else tracking the top.
    private float mCurrentScaleProgress;
    private SpringAnimation mRectXSpring;
    private SpringAnimation mRectYSpring;
    private SpringAnimation mRectScaleAnim;
    private boolean mAnimsStarted;
    private boolean mRectXAnimEnded;
    private boolean mRectYAnimEnded;
    private boolean mRectScaleAnimEnded;

    // trebufork: watchdog against a stuck spring. The close-to-icon spring can fail to complete
    // (e.g. the scale spring's end-listener never fires after a re-target storm, or the scrollable
    // home list keeps scrolling), so onAnimationEnd never runs and the FloatingIconView leaks on
    // the workspace. Force-end the animation once it has been idle for WATCHDOG_DELAY_MS.
    private static final long WATCHDOG_DELAY_MS = 3000L;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mWatchdogRunnable = () -> {
        if (mAnimsStarted && !isEnded()) {
            android.util.Log.d(TREBUFORK_TAG,
                    "RectFSpringAnim.watchdog: forcing end (animation did not complete)");
            end();
        }
    };

    /**
     * Indicates which part of the start & target rects we are interpolating between.
     */
    public static final int TRACKING_TOP = 0;
    public static final int TRACKING_CENTER = 1;
    public static final int TRACKING_BOTTOM = 2;

    @Retention(SOURCE)
    @IntDef(value = {TRACKING_TOP,
                    TRACKING_CENTER,
                    TRACKING_BOTTOM})
    public @interface Tracking{}

    @Tracking
    public final int mTracking;
    protected final float mStiffnessX;
    protected final float mStiffnessY;
    protected final float mDampingX;
    protected final float mDampingY;
    protected final float mRectStiffness;

    public RectFSpringAnim(SpringConfig config) {
        mStartRect = config.startRect;
        mTargetRect = config.targetRect;
        mCurrentCenterX = mStartRect.centerX();

        setCanRelease(true);

        mTracking = config.tracking;
        mStiffnessX = config.stiffnessX;
        mStiffnessY = config.stiffnessY;
        mDampingX = config.dampingX;
        mDampingY = config.dampingY;
        mRectStiffness = config.rectStiffness;

        mCurrentY = getTrackedYFromRect(mStartRect);
    }

    public RectF getTargetRect() {
        return mTargetRect;
    }

    private float getTrackedYFromRect(RectF rect) {
        switch (mTracking) {
            case TRACKING_TOP:
                return rect.top;
            case TRACKING_BOTTOM:
                return rect.bottom;
            case TRACKING_CENTER:
            default:
                return rect.centerY();
        }
    }

    public void onTargetPositionChanged() {
        if (isEnded()) {
            return;
        }
        // trebufork: log re-targets (icon tracking during scrollable-home scroll). Bounded by the
        // update listener rate; only fires when the icon actually moved.
        android.util.Log.d(TREBUFORK_TAG, "onTargetPositionChanged: re-targeting springs to "
                + mTargetRect + " animsStarted=" + mAnimsStarted
                + " xEnded=" + mRectXAnimEnded + " yEnded=" + mRectYAnimEnded
                + " scaleEnded=" + mRectScaleAnimEnded);

        if (mRectXSpring != null) {
            mRectXSpring.animateToFinalPosition(mTargetRect.centerX());
            mRectXAnimEnded = false;
        }

        if (mRectYSpring != null) {
            switch (mTracking) {
                case TRACKING_TOP:
                    mRectYSpring.animateToFinalPosition(mTargetRect.top);
                    break;
                case TRACKING_BOTTOM:
                    mRectYSpring.animateToFinalPosition(mTargetRect.bottom);
                    break;
                case TRACKING_CENTER:
                    mRectYSpring.animateToFinalPosition(mTargetRect.centerY());
                    break;
            }
            mRectYAnimEnded = false;
        }
        // trebufork: re-arm the watchdog — the springs are being actively re-targeted, so they
        // must get another grace period before the force-end kicks in.
        mHandler.removeCallbacks(mWatchdogRunnable);
        mHandler.postDelayed(mWatchdogRunnable, WATCHDOG_DELAY_MS);
    }

    public void addOnUpdateListener(OnUpdateListener onUpdateListener) {
        mOnUpdateListeners.add(onUpdateListener);
    }

    public void addAnimatorListener(Animator.AnimatorListener animatorListener) {
        mAnimatorListeners.add(animatorListener);
    }

    /**
     * Starts the fling/spring animation.
     * @param context The activity context.
     * @param velocityPxPerMs Velocity of swipe in px/ms.
     */
    public void start(Context context, @Nullable DeviceProfile profile, PointF velocityPxPerMs) {
        // Only tell caller that we ended if both x and y animations have ended.
        OnAnimationEndListener onXEndListener = ((animation, canceled, centerX, velocityX) -> {
            mRectXAnimEnded = true;
            maybeOnEnd();
        });
        OnAnimationEndListener onYEndListener = ((animation, canceled, centerY, velocityY) -> {
            mRectYAnimEnded = true;
            maybeOnEnd();
        });

        float xVelocityPxPerS = velocityPxPerMs.x * 1000;
        float yVelocityPxPerS = velocityPxPerMs.y * 1000;
        float startX = mCurrentCenterX;
        float endX = mTargetRect.centerX();
        float startY = mCurrentY;
        float endY = getTrackedYFromRect(mTargetRect);
        // trebufork: never fly toward an unpopulated (empty) target rect - if the closing
        // icon's bounds were not yet available when this spring started, baking centerX()/Y of
        // a RectF(0,0,0,0) makes the window fly to the top/left corner on screen. Hold position
        // instead; FloatingIconView fires onTargetPositionChanged as soon as the icon lays out,
        // which re-anchors these springs to the real icon location.
        if (mTargetRect.isEmpty()) {
            endX = startX;
            endY = startY;
            android.util.Log.d(TREBUFORK_TAG, "start: empty target - holding position ("
                    + mTargetRect + ") until a re-target anchors the icon");
        }
        float minVisibleChange = Math.abs(1f / mStartRect.height());

        ResourceProvider rp = DynamicResource.provider(context);
        long minVelocityXPxPerS = rp.getInt(R.dimen.swipe_up_min_velocity_x_px_per_s);
        long maxVelocityXPxPerS = rp.getInt(R.dimen.swipe_up_max_velocity_x_px_per_s);
        long minVelocityYPxPerS = rp.getInt(R.dimen.swipe_up_min_velocity_y_px_per_s);
        long maxVelocityYPxPerS = rp.getInt(R.dimen.swipe_up_max_velocity_y_px_per_s);
        float fallOffFactor = rp.getFloat(R.dimen.swipe_up_max_velocity_fall_off_factor);

        // We want the actual initial velocity to never dip below the minimum, and to taper off
        // once it's above the soft cap so that we can prevent the window from flying off
        // screen, while maintaining a natural feel.
        xVelocityPxPerS = adjustVelocity(
                xVelocityPxPerS, minVelocityXPxPerS, maxVelocityXPxPerS, fallOffFactor);
        yVelocityPxPerS = adjustVelocity(
                yVelocityPxPerS, minVelocityYPxPerS, maxVelocityYPxPerS, fallOffFactor);

        // trebufork: master launcher animation speed - spring duration ~ 1/sqrt(stiffness), so
        // scaling stiffness by speed^2 (same damping ratio) makes these springs speedx faster.
        float animStiffnessScale = AnimationSpeed.getSpringStiffnessScale(context);
        float stiffnessX = rp.getFloat(R.dimen.swipe_up_rect_x_stiffness) * animStiffnessScale;
        float dampingX = rp.getFloat(R.dimen.swipe_up_rect_x_damping_ratio);
        mRectXSpring =
                new SpringAnimation(this, RECT_CENTER_X)
                        .setSpring(
                                new SpringForce(endX)
                                        .setStiffness(stiffnessX)
                                        .setDampingRatio(dampingX)
                        ).setStartValue(startX)
                        .setStartVelocity(xVelocityPxPerS)
                        .addEndListener(onXEndListener);

        float stiffnessY = rp.getFloat(R.dimen.swipe_up_rect_y_stiffness) * animStiffnessScale;
        float dampingY = rp.getFloat(R.dimen.swipe_up_rect_y_damping_ratio);
        mRectYSpring =
                new SpringAnimation(this, RECT_Y)
                        .setSpring(
                                new SpringForce(endY)
                                        .setStiffness(stiffnessY)
                                        .setDampingRatio(dampingY)
                        )
                        .setStartValue(startY)
                        .setStartVelocity(yVelocityPxPerS)
                        .addEndListener(onYEndListener);

        float stiffnessZ = rp.getFloat(R.dimen.swipe_up_rect_scale_stiffness_v2) * animStiffnessScale;
        float dampingZ = rp.getFloat(R.dimen.swipe_up_rect_scale_damping_ratio_v2);
        mRectScaleAnim =
                new SpringAnimation(this, RECT_SCALE_PROGRESS)
                        .setSpring(
                                new SpringForce(1f)
                                        .setStiffness(stiffnessZ)
                                        .setDampingRatio(dampingZ))
                        .setStartVelocity(velocityPxPerMs.y * minVisibleChange)
                        .setMaxValue(1f)
                        .setMinimumVisibleChange(minVisibleChange)
                        .addEndListener((animation, canceled, value, velocity) -> {
                            mRectScaleAnimEnded = true;
                            maybeOnEnd();
                        });

        setCanRelease(false);
        mAnimsStarted = true;
        // trebufork: arm the watchdog; it is re-armed on each re-target so a legitimately
        // tracking icon (list scroll) is not cut short, but a spring that stops being driven
        // without completing is force-ended and its floating icon removed.
        mHandler.removeCallbacks(mWatchdogRunnable);
        mHandler.postDelayed(mWatchdogRunnable, WATCHDOG_DELAY_MS);
        android.util.Log.d(TREBUFORK_TAG, "RectFSpringAnim.start: startRect=" + mStartRect
                + " targetRect=" + mTargetRect + " velocityX=" + xVelocityPxPerS
                + " velocityY=" + yVelocityPxPerS + " listeners=" + mAnimatorListeners.size());

        mRectXSpring.start();
        mRectYSpring.start();

        mRectScaleAnim.start();
        for (Animator.AnimatorListener animatorListener : mAnimatorListeners) {
            animatorListener.onAnimationStart(null);
        }
    }

    public void end() {
        android.util.Log.d(TREBUFORK_TAG, "RectFSpringAnim.end: animsStarted=" + mAnimsStarted
                + " xEnded=" + mRectXAnimEnded + " yEnded=" + mRectYAnimEnded
                + " scaleEnded=" + mRectScaleAnimEnded);
        mHandler.removeCallbacks(mWatchdogRunnable);
        if (mAnimsStarted) {
            if (mRectXSpring.canSkipToEnd()) {
                mRectXSpring.skipToEnd();
            }
            if (mRectYSpring.canSkipToEnd()) {
                mRectYSpring.skipToEnd();
            }
            if (mRectScaleAnim.canSkipToEnd()) {
                mRectScaleAnim.skipToEnd();
            }
            mCurrentScaleProgress = mRectScaleAnim.getSpring().getFinalPosition();

            // Ensures that we end the animation with the final values.
            mRectXAnimEnded = false;
            mRectYAnimEnded = false;
            mRectScaleAnimEnded = false;
            onUpdate();
        }

        mRectXAnimEnded = true;
        mRectYAnimEnded = true;
        mRectScaleAnimEnded = true;
        maybeOnEnd();
    }

    private boolean isEnded() {
        return mRectXAnimEnded && mRectYAnimEnded && mRectScaleAnimEnded;
    }

    private void onUpdate() {
        if (isEnded()) {
            // Prevent further updates from being called. This can happen between callbacks for
            // ending the x/y/scale animations.
            return;
        }

        if (!mOnUpdateListeners.isEmpty()) {
            float currentWidth = Utilities.mapRange(mCurrentScaleProgress, mStartRect.width(),
                    mTargetRect.width());
            float currentHeight = Utilities.mapRange(mCurrentScaleProgress, mStartRect.height(),
                    mTargetRect.height());
            switch (mTracking) {
                case TRACKING_TOP:
                    mCurrentRect.set(mCurrentCenterX - currentWidth / 2,
                            mCurrentY,
                            mCurrentCenterX + currentWidth / 2,
                            mCurrentY + currentHeight);
                    break;
                case TRACKING_BOTTOM:
                    mCurrentRect.set(mCurrentCenterX - currentWidth / 2,
                            mCurrentY - currentHeight,
                            mCurrentCenterX + currentWidth / 2,
                            mCurrentY);
                    break;
                case TRACKING_CENTER:
                    mCurrentRect.set(mCurrentCenterX - currentWidth / 2,
                            mCurrentY - currentHeight / 2,
                            mCurrentCenterX + currentWidth / 2,
                            mCurrentY + currentHeight / 2);
                    break;
            }
            for (OnUpdateListener onUpdateListener : mOnUpdateListeners) {
                onUpdateListener.onUpdate(mCurrentRect, mCurrentScaleProgress);
            }
        }
    }

    private void maybeOnEnd() {
        // trebufork: log every settle attempt — if this never fires onAnimationEnd the floating
        // icon leaks (stuck circle on the workspace).
        android.util.Log.d(TREBUFORK_TAG, "maybeOnEnd: animsStarted=" + mAnimsStarted
                + " xEnded=" + mRectXAnimEnded + " yEnded=" + mRectYAnimEnded
                + " scaleEnded=" + mRectScaleAnimEnded
                + " firing=" + (mAnimsStarted && isEnded())
                + " listeners=" + mAnimatorListeners.size());
        if (mAnimsStarted && isEnded()) {
            mAnimsStarted = false;
            mHandler.removeCallbacks(mWatchdogRunnable);
            setCanRelease(true);
            for (Animator.AnimatorListener animatorListener : mAnimatorListeners) {
                animatorListener.onAnimationEnd(null);
            }
        }
    }

    public void cancel() {
        android.util.Log.d(TREBUFORK_TAG, "RectFSpringAnim.cancel: animsStarted=" + mAnimsStarted);
        if (mAnimsStarted) {
            for (OnUpdateListener onUpdateListener : mOnUpdateListeners) {
                onUpdateListener.onCancel();
            }
        }
        end();
    }

    /**
     * Modify the given velocity so that it's never below the minimum value, and falls off by the
     * given factor once it goes above the maximum value.
     * In order for the max soft cap to be enforced, the fall-off factor must be >1.
     */
    private static float adjustVelocity(float velocity, long min, long max, float factor) {
        float sign = Math.signum(velocity);
        float magnitude = Math.abs(velocity);

        // If the absolute velocity is less than the min, bump it up.
        if (magnitude < min) {
            return min * sign;
        }

        // If the absolute velocity falls between min and max, or the fall-off factor is invalid,
        // do nothing.
        if (magnitude <= max || factor <= 1) {
            return velocity;
        }

        // Scale the excess velocity by the fall-off factor.
        float excess = magnitude - max;
        float scaled = (float) Math.pow(excess, 1f / factor);
        return (max + scaled) * sign;
    }

    public interface OnUpdateListener {
        /**
         * Called when an update is made to the animation.
         * @param currentRect The rect of the window.
         * @param progress [0, 1] The progress of the rect scale animation.
         */
        void onUpdate(RectF currentRect, float progress);

        default void onCancel() { }
    }

    private abstract static class SpringConfig {
        protected RectF startRect;
        protected RectF targetRect;
        protected @Tracking int tracking;
        protected float stiffnessX;
        protected float stiffnessY;
        protected float dampingX;
        protected float dampingY;
        protected float rectStiffness;
        protected float minVisChange;
        protected int maxVelocityPxPerS;

        private SpringConfig(Context context, RectF start, RectF target) {
            startRect = start;
            targetRect = target;

            ResourceProvider rp = DynamicResource.provider(context);
            minVisChange = rp.getDimension(R.dimen.swipe_up_fling_min_visible_change);
            maxVelocityPxPerS = (int) rp.getDimension(R.dimen.swipe_up_max_velocity);
        }
    }

    /**
     * Standard spring configuration parameters.
     */
    public static class DefaultSpringConfig extends SpringConfig {

        public DefaultSpringConfig(Context context, DeviceProfile deviceProfile,
                RectF startRect, RectF targetRect) {
            super(context, startRect, targetRect);

            ResourceProvider rp = DynamicResource.provider(context);
            tracking = getDefaultTracking(deviceProfile);
            // trebufork: master launcher animation speed - scale spring stiffness by speed^2.
            float animStiffnessScale = AnimationSpeed.getSpringStiffnessScale(context);
            stiffnessX = rp.getFloat(R.dimen.swipe_up_rect_xy_stiffness) * animStiffnessScale;
            stiffnessY = rp.getFloat(R.dimen.swipe_up_rect_xy_stiffness) * animStiffnessScale;
            dampingX = rp.getFloat(R.dimen.swipe_up_rect_xy_damping_ratio);
            dampingY = rp.getFloat(R.dimen.swipe_up_rect_xy_damping_ratio);

            this.startRect = startRect;
            this.targetRect = targetRect;

            // Increase the stiffness for devices where we want the window size to transform
            // quicker.
            boolean shouldUseHigherStiffness = deviceProfile != null
                    && (deviceProfile.getDeviceProperties().isLandscape() || deviceProfile.getDeviceProperties().isTablet());
            rectStiffness = (shouldUseHigherStiffness
                    ? rp.getFloat(R.dimen.swipe_up_rect_scale_higher_stiffness)
                    : rp.getFloat(R.dimen.swipe_up_rect_scale_stiffness)) * animStiffnessScale;
        }

        private @Tracking int getDefaultTracking(@Nullable DeviceProfile deviceProfile) {
            @Tracking int tracking;
            if (deviceProfile == null) {
                tracking = startRect.bottom < targetRect.bottom
                        ? TRACKING_BOTTOM
                        : TRACKING_TOP;
            } else {
                int heightPx = deviceProfile.getDeviceProperties().getHeightPx();
                Rect padding = deviceProfile.mWorkspaceProfile.getWorkspacePadding();

                final float topThreshold = heightPx / 3f;
                final float bottomThreshold = deviceProfile.getDeviceProperties().getHeightPx() - padding.bottom;

                if (targetRect.bottom > bottomThreshold) {
                    tracking = TRACKING_CENTER;
                } else if (targetRect.top < topThreshold) {
                    tracking = TRACKING_TOP;
                } else {
                    tracking = TRACKING_CENTER;
                }
            }
            return tracking;
        }
    }

    /**
     * Spring configuration parameters for Taskbar/Hotseat items on devices that have a taskbar.
     */
    public static class TaskbarHotseatSpringConfig extends SpringConfig {

        public TaskbarHotseatSpringConfig(Context context, RectF start, RectF target) {
            super(context, start, target);

            ResourceProvider rp = DynamicResource.provider(context);
            tracking = TRACKING_CENTER;
            // trebufork: master launcher animation speed - scale spring stiffness by speed^2.
            float animStiffnessScale = AnimationSpeed.getSpringStiffnessScale(context);
            stiffnessX = rp.getFloat(R.dimen.taskbar_swipe_up_rect_x_stiffness) * animStiffnessScale;
            stiffnessY = rp.getFloat(R.dimen.taskbar_swipe_up_rect_y_stiffness) * animStiffnessScale;
            dampingX = rp.getFloat(R.dimen.taskbar_swipe_up_rect_x_damping);
            dampingY = rp.getFloat(R.dimen.taskbar_swipe_up_rect_y_damping);
            rectStiffness = rp.getFloat(R.dimen.taskbar_swipe_up_rect_scale_stiffness) * animStiffnessScale;
        }
    }

}
