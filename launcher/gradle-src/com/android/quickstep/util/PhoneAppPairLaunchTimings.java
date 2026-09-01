/*
 * Gradle migration compatibility implementation.
 *
 * Kotlin 2.2 K2/KAPT currently crashes while building fake overrides for the
 * original Kotlin subclass of the Java SplitAnimationTimings interface. The
 * Soong source remains unchanged; this Java equivalent is used only here.
 */
package com.android.quickstep.util;

import com.android.app.animation.Interpolators;

public final class PhoneAppPairLaunchTimings extends AppPairLaunchTimings {
    @Override
    protected int getSTAGED_RECT_SLIDE_DURATION() {
        return 500;
    }

    @Override
    public int getDuration() {
        return SplitAnimationTimings.PHONE_APP_PAIR_LAUNCH_DURATION;
    }
}
