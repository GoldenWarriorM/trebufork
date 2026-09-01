/*
 * trebufork: master launcher animation speed.
 *
 * The user picks 0.5x..2x in Settings (LauncherPrefs.ANIMATION_SPEED, an integer percent).
 * Only the main launcher animations honour it:
 *   - Animator/AnimatorSet based ones (app open/close, widget launches) are scaled through
 *     AnimationResult.setAnimation via {@link #applyDurationScale}.
 *   - Spring based ones (recents, close-to-icon, workspace settle) are scaled through
 *     {@link #getStiffnessScale} (duration of a spring at constant damping is ~ 1/sqrt(stiffness),
 *     so step/stiffness factor = speed^2 keeps the same relative dynamics, just faster/slower).
 *
 * The control is OFF (animations run at their stock speed, setting shown disabled) whenever the
 * user has changed any of the Developer-options animation scales away from 1x - the system-level
 * override then takes precedence.
 */
package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;

public final class AnimationSpeed {

    private AnimationSpeed() {
    }

    /** The chosen launcher animation speed factor (1.0 = stock). */
    public static float getSpeed(Context context) {
        String v = LauncherPrefs.getPrefs(context)
                .getString(LauncherPrefs.ANIMATION_SPEED.getSharedPrefKey(), "100");
        try {
            return Math.max(0.1f, Integer.parseInt(v.trim())) / 100f;
        } catch (NumberFormatException e) {
            return 1f;
        }
    }

    /**
     * True if any Developer-options animation scale (animator / transition / window) has been
     * changed away from 1x. In that case the launcher speed control is ignored/disabled.
     */
    public static boolean isOverriddenByDeveloperOptions(Context context) {
        RemoveAnimationSettingsTracker tracker = RemoveAnimationSettingsTracker.INSTANCE.get(context);
        return tracker.getValue(RemoveAnimationSettingsTracker.WINDOW_ANIMATION_SCALE_URI) != 1f
                || tracker.getValue(RemoveAnimationSettingsTracker.TRANSITION_ANIMATION_SCALE_URI) != 1f
                || tracker.getValue(RemoveAnimationSettingsTracker.ANIMATOR_DURATION_SCALE_URI) != 1f;
    }

    /**
     * Animator instances already scaled by {@link #applyDurationScale}, so a later call on the
     * same instance (e.g. the recents-reverse tree scaled inside composeRecentsLaunchAnimator and
     * then scaled again by LauncherAnimationRunner.setAnimation) is a no-op instead of doubling
     * the factor. Weak refs: animators are composed fresh per transition and discarded.
     */
    private static final java.util.WeakHashMap<Animator, Boolean> sScaled =
            new java.util.WeakHashMap<>();

    /** Scales an animator tree's durations (proportionally to each child) by the chosen speed. */
    public static void applyDurationScale(Context context, Animator animator) {
        if (animator == null || isOverriddenByDeveloperOptions(context)) {
            return;
        }
        float speed = getSpeed(context);
        if (Math.abs(speed - 1f) < 0.001f) {
            return;
        }
        synchronized (sScaled) {
            if (sScaled.containsKey(animator)) {
                return;
            }
            sScaled.put(animator, Boolean.TRUE);
        }
        applyDurationScaleRecursive(animator, 1f / speed);
    }

    private static void applyDurationScaleRecursive(Animator animator, float factor) {
        if (animator instanceof AnimatorSet) {
            synchronized (sScaled) {
                if (sScaled.containsKey(animator)) {
                    return;
                }
                sScaled.put(animator, Boolean.TRUE);
            }
            for (Animator child : ((AnimatorSet) animator).getChildAnimations()) {
                applyDurationScaleRecursive(child, factor);
            }
        } else {
            long duration = animator.getDuration();
            if (duration > 0) {
                animator.setDuration(Math.max(1L, Math.round(duration * factor)));
            }
        }
    }

    /**
     * Spring stiffness multiplier for the chosen speed: a spring's duration (at constant damping
     * ratio) scales ~ 1/sqrt(stiffness), so to make the animation {@code speed}x faster we must
     * multiply stiffness by {@code speed * speed}. Returns 1 when the control is off.
     */
    public static float getSpringStiffnessScale(Context context) {
        if (isOverriddenByDeveloperOptions(context)) {
            return 1f;
        }
        float speed = getSpeed(context);
        return Math.abs(speed - 1f) < 0.001f ? 1f : speed * speed;
    }
}