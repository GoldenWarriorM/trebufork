package com.android.quickstep.util;

/** Gradle-only Java equivalent of the Kotlin Soong implementation. */
public final class TabletAppPairLaunchTimings extends AppPairLaunchTimings {
    @Override
    protected int getSTAGED_RECT_SLIDE_DURATION() {
        return 600;
    }

    @Override
    public int getDuration() {
        return SplitAnimationTimings.TABLET_APP_PAIR_LAUNCH_DURATION;
    }
}
