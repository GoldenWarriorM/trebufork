package com.android.wm.shell;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean bugRotationButtonCoverBubble = true;
    private static boolean dismissPipFromLockscreen = true;
    private static boolean enable2x1Split = false;
    private static boolean enableAutoTaskStackController = true;
    private static boolean enableBubbleAnything = false;
    private static boolean enableBubbleBar = true;
    private static boolean enableBubbleBarOnPhones = false;
    private static boolean enableBubbleBarToFloatingTransition = true;
    private static boolean enableBubbleEventHistoryLogs = false;
    private static boolean enableBubbleStashing = true;
    private static boolean enableBubbleToFullscreen = false;
    private static boolean enableBubblesLongPressNavHandle = false;
    private static boolean enableCreateAnyBubble = true;
    private static boolean enableDynamicInsetsForAppLaunch = true;
    private static boolean enableFlexibleSplit = false;
    private static boolean enableFlexibleTwoAppSplit = true;
    private static boolean enableGsf = true;
    private static boolean enableMagneticSplitDivider = false;
    private static boolean enableNewBubbleAnimations = false;
    private static boolean enableOptionalBubbleOverflow = false;
    private static boolean enablePip2 = true;
    private static boolean enablePip2OnTv = false;
    private static boolean enablePipBoxShadows = true;
    private static boolean enablePipUmoExperience = false;
    private static boolean enableRetrievableBubbles = false;
    private static boolean enableShellRestartBubbleCleanup = true;
    private static boolean enableShellTopTaskTracking = false;
    private static boolean enableTaskbarNavbarUnification = true;
    private static boolean enableTaskbarOnPhones = true;
    private static boolean enableTinyTaskbar = false;
    private static boolean fixBubbleStackViewExpandedWhenAdded = false;
    private static boolean fixBubblesAddSameBubbleBeingRemoved = false;
    private static boolean fixBubblesCancelAnimation = true;
    private static boolean fixBubblesExpandedSysuiFlag = true;
    private static boolean fixBubblesImeFocusFlicker = false;
    private static boolean fixExitSplitOnEnterBubble = true;
    private static boolean fixMissingUserChangeCallbacks = true;
    private static boolean fixTaskViewRotationAnimation = true;
    private static boolean splitDisableChildTaskBounds = true;
    private static boolean splitToFullSetWindowMode = false;
    private static boolean taskViewTransitionsRefactor = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("com.android.wm.shell", 0x8D31038CB242903FL);
            bugRotationButtonCoverBubble = reader.getBooleanFlagValue(0);
            dismissPipFromLockscreen = reader.getBooleanFlagValue(1);
            enable2x1Split = reader.getBooleanFlagValue(2);
            enableAutoTaskStackController = reader.getBooleanFlagValue(3);
            enableBubbleAnything = reader.getBooleanFlagValue(4);
            enableBubbleBar = reader.getBooleanFlagValue(5);
            enableBubbleBarOnPhones = reader.getBooleanFlagValue(6);
            enableBubbleBarToFloatingTransition = reader.getBooleanFlagValue(7);
            enableBubbleEventHistoryLogs = reader.getBooleanFlagValue(8);
            enableBubbleStashing = reader.getBooleanFlagValue(9);
            enableBubbleToFullscreen = reader.getBooleanFlagValue(10);
            enableBubblesLongPressNavHandle = reader.getBooleanFlagValue(11);
            enableCreateAnyBubble = reader.getBooleanFlagValue(12);
            enableDynamicInsetsForAppLaunch = reader.getBooleanFlagValue(13);
            enableFlexibleSplit = reader.getBooleanFlagValue(14);
            enableFlexibleTwoAppSplit = reader.getBooleanFlagValue(15);
            enableGsf = reader.getBooleanFlagValue(16);
            enableMagneticSplitDivider = reader.getBooleanFlagValue(17);
            enableNewBubbleAnimations = reader.getBooleanFlagValue(18);
            enableOptionalBubbleOverflow = reader.getBooleanFlagValue(19);
            enablePip2 = reader.getBooleanFlagValue(20);
            enablePip2OnTv = reader.getBooleanFlagValue(21);
            enablePipBoxShadows = reader.getBooleanFlagValue(22);
            enablePipUmoExperience = reader.getBooleanFlagValue(23);
            enableRetrievableBubbles = reader.getBooleanFlagValue(24);
            enableShellRestartBubbleCleanup = reader.getBooleanFlagValue(25);
            enableShellTopTaskTracking = reader.getBooleanFlagValue(26);
            enableTaskbarNavbarUnification = reader.getBooleanFlagValue(27);
            enableTaskbarOnPhones = reader.getBooleanFlagValue(28);
            enableTinyTaskbar = reader.getBooleanFlagValue(29);
            fixBubbleStackViewExpandedWhenAdded = reader.getBooleanFlagValue(30);
            fixBubblesAddSameBubbleBeingRemoved = reader.getBooleanFlagValue(31);
            fixBubblesCancelAnimation = reader.getBooleanFlagValue(32);
            fixBubblesExpandedSysuiFlag = reader.getBooleanFlagValue(33);
            fixBubblesImeFocusFlicker = reader.getBooleanFlagValue(34);
            fixExitSplitOnEnterBubble = reader.getBooleanFlagValue(35);
            fixMissingUserChangeCallbacks = reader.getBooleanFlagValue(36);
            fixTaskViewRotationAnimation = reader.getBooleanFlagValue(37);
            splitDisableChildTaskBounds = reader.getBooleanFlagValue(38);
            splitToFullSetWindowMode = reader.getBooleanFlagValue(39);
            taskViewTransitionsRefactor = reader.getBooleanFlagValue(40);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } catch (LinkageError e) {
            // for mainline module running on older devices.
            // This should be replaces to version check, after the version bump.
            Log.e(TAG, e.toString());
        }
        isCached = true;
    }
    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bugRotationButtonCoverBubble() {
        if (!isCached) {
            init();
        }
        return bugRotationButtonCoverBubble;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dismissPipFromLockscreen() {
        if (!isCached) {
            init();
        }
        return dismissPipFromLockscreen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enable2x1Split() {
        if (!isCached) {
            init();
        }
        return enable2x1Split;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAutoTaskStackController() {
        if (!isCached) {
            init();
        }
        return enableAutoTaskStackController;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleAnything() {
        if (!isCached) {
            init();
        }
        return enableBubbleAnything;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleBar() {
        if (!isCached) {
            init();
        }
        return enableBubbleBar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleBarOnPhones() {
        if (!isCached) {
            init();
        }
        return enableBubbleBarOnPhones;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleBarToFloatingTransition() {
        if (!isCached) {
            init();
        }
        return enableBubbleBarToFloatingTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleEventHistoryLogs() {
        if (!isCached) {
            init();
        }
        return enableBubbleEventHistoryLogs;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleStashing() {
        if (!isCached) {
            init();
        }
        return enableBubbleStashing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubbleToFullscreen() {
        if (!isCached) {
            init();
        }
        return enableBubbleToFullscreen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBubblesLongPressNavHandle() {
        if (!isCached) {
            init();
        }
        return enableBubblesLongPressNavHandle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCreateAnyBubble() {
        if (!isCached) {
            init();
        }
        return enableCreateAnyBubble;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDynamicInsetsForAppLaunch() {
        if (!isCached) {
            init();
        }
        return enableDynamicInsetsForAppLaunch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFlexibleSplit() {
        if (!isCached) {
            init();
        }
        return enableFlexibleSplit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFlexibleTwoAppSplit() {
        if (!isCached) {
            init();
        }
        return enableFlexibleTwoAppSplit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableGsf() {
        if (!isCached) {
            init();
        }
        return enableGsf;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMagneticSplitDivider() {
        if (!isCached) {
            init();
        }
        return enableMagneticSplitDivider;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNewBubbleAnimations() {
        if (!isCached) {
            init();
        }
        return enableNewBubbleAnimations;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOptionalBubbleOverflow() {
        if (!isCached) {
            init();
        }
        return enableOptionalBubbleOverflow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePip2() {
        if (!isCached) {
            init();
        }
        return enablePip2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePip2OnTv() {
        if (!isCached) {
            init();
        }
        return enablePip2OnTv;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePipBoxShadows() {
        if (!isCached) {
            init();
        }
        return enablePipBoxShadows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePipUmoExperience() {
        if (!isCached) {
            init();
        }
        return enablePipUmoExperience;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRetrievableBubbles() {
        if (!isCached) {
            init();
        }
        return enableRetrievableBubbles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableShellRestartBubbleCleanup() {
        if (!isCached) {
            init();
        }
        return enableShellRestartBubbleCleanup;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableShellTopTaskTracking() {
        if (!isCached) {
            init();
        }
        return enableShellTopTaskTracking;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarNavbarUnification() {
        if (!isCached) {
            init();
        }
        return enableTaskbarNavbarUnification;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarOnPhones() {
        if (!isCached) {
            init();
        }
        return enableTaskbarOnPhones;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTinyTaskbar() {
        if (!isCached) {
            init();
        }
        return enableTinyTaskbar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubbleStackViewExpandedWhenAdded() {
        if (!isCached) {
            init();
        }
        return fixBubbleStackViewExpandedWhenAdded;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubblesAddSameBubbleBeingRemoved() {
        if (!isCached) {
            init();
        }
        return fixBubblesAddSameBubbleBeingRemoved;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubblesCancelAnimation() {
        if (!isCached) {
            init();
        }
        return fixBubblesCancelAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubblesExpandedSysuiFlag() {
        if (!isCached) {
            init();
        }
        return fixBubblesExpandedSysuiFlag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubblesImeFocusFlicker() {
        if (!isCached) {
            init();
        }
        return fixBubblesImeFocusFlicker;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixExitSplitOnEnterBubble() {
        if (!isCached) {
            init();
        }
        return fixExitSplitOnEnterBubble;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixMissingUserChangeCallbacks() {
        if (!isCached) {
            init();
        }
        return fixMissingUserChangeCallbacks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixTaskViewRotationAnimation() {
        if (!isCached) {
            init();
        }
        return fixTaskViewRotationAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean splitDisableChildTaskBounds() {
        if (!isCached) {
            init();
        }
        return splitDisableChildTaskBounds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean splitToFullSetWindowMode() {
        if (!isCached) {
            init();
        }
        return splitToFullSetWindowMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean taskViewTransitionsRefactor() {
        if (!isCached) {
            init();
        }
        return taskViewTransitionsRefactor;
    }

}
