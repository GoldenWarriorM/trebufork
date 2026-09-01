package com.android.systemui.shared;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean ambientAod = true;
    private static boolean bouncerAreaExclusion = true;
    private static boolean clockReactiveSmartspaceLayout = true;
    private static boolean clockReactiveVariants = true;
    private static boolean cursorHotCorner = true;
    private static boolean enableHomeDelay = false;
    private static boolean enableLppAssistInvocationEffect = true;
    private static boolean enableLppAssistInvocationHapticEffect = true;
    private static boolean exampleSharedFlag = false;
    private static boolean extendedWallpaperEffects = true;
    private static boolean extendibleThemeManager = true;
    private static boolean lockscreenCustomClocks = true;
    private static boolean newCustomizationPickerUi = true;
    private static boolean newTouchpadGesturesTutorial = true;
    private static boolean notificationDotContrastBorder = true;
    private static boolean panAndZoomInExtendedWallpaperEffects = false;
    private static boolean screenshotContextUrl = true;
    private static boolean shadeAllowBackGesture = false;
    private static boolean sidefpsControllerRefactor = true;
    private static boolean smartspaceAqiUpdatedDesign = false;
    private static boolean smartspaceSemanticWeatherData = false;
    private static boolean smartspaceSportsCardBackground = false;
    private static boolean smartspaceUiUpdate = true;
    private static boolean smartspaceWeatherUseMonochromeFontIcons = false;
    private static boolean statusBarConnectedDisplays = true;
    private static boolean threeButtonCornerSwipe = false;
    private static boolean usePreferredImageEditor = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("com.android.systemui.shared", 0x10B202E4B6E25E54L);
            sidefpsControllerRefactor = reader.getBooleanFlagValue(18);
            extendibleThemeManager = reader.getBooleanFlagValue(10);
            notificationDotContrastBorder = reader.getBooleanFlagValue(14);
            statusBarConnectedDisplays = reader.getBooleanFlagValue(25);
            ambientAod = reader.getBooleanFlagValue(0);
            bouncerAreaExclusion = reader.getBooleanFlagValue(1);
            clockReactiveSmartspaceLayout = reader.getBooleanFlagValue(2);
            clockReactiveVariants = reader.getBooleanFlagValue(3);
            cursorHotCorner = reader.getBooleanFlagValue(4);
            enableHomeDelay = reader.getBooleanFlagValue(5);
            enableLppAssistInvocationEffect = reader.getBooleanFlagValue(6);
            enableLppAssistInvocationHapticEffect = reader.getBooleanFlagValue(7);
            exampleSharedFlag = reader.getBooleanFlagValue(8);
            extendedWallpaperEffects = reader.getBooleanFlagValue(9);
            lockscreenCustomClocks = reader.getBooleanFlagValue(11);
            newCustomizationPickerUi = reader.getBooleanFlagValue(12);
            newTouchpadGesturesTutorial = reader.getBooleanFlagValue(13);
            panAndZoomInExtendedWallpaperEffects = reader.getBooleanFlagValue(15);
            screenshotContextUrl = reader.getBooleanFlagValue(16);
            shadeAllowBackGesture = reader.getBooleanFlagValue(17);
            smartspaceAqiUpdatedDesign = reader.getBooleanFlagValue(19);
            smartspaceSemanticWeatherData = reader.getBooleanFlagValue(20);
            smartspaceSportsCardBackground = reader.getBooleanFlagValue(21);
            smartspaceUiUpdate = reader.getBooleanFlagValue(22);
            smartspaceWeatherUseMonochromeFontIcons = reader.getBooleanFlagValue(24);
            threeButtonCornerSwipe = reader.getBooleanFlagValue(26);
            usePreferredImageEditor = reader.getBooleanFlagValue(27);
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

    public boolean ambientAod() {
        if (!isCached) {
            init();
        }
        return ambientAod;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bouncerAreaExclusion() {
        if (!isCached) {
            init();
        }
        return bouncerAreaExclusion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clockReactiveSmartspaceLayout() {
        if (!isCached) {
            init();
        }
        return clockReactiveSmartspaceLayout;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clockReactiveVariants() {
        if (!isCached) {
            init();
        }
        return clockReactiveVariants;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cursorHotCorner() {
        if (!isCached) {
            init();
        }
        return cursorHotCorner;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHomeDelay() {
        if (!isCached) {
            init();
        }
        return enableHomeDelay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLppAssistInvocationEffect() {
        if (!isCached) {
            init();
        }
        return enableLppAssistInvocationEffect;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLppAssistInvocationHapticEffect() {
        if (!isCached) {
            init();
        }
        return enableLppAssistInvocationHapticEffect;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean exampleSharedFlag() {
        if (!isCached) {
            init();
        }
        return exampleSharedFlag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean extendedWallpaperEffects() {
        if (!isCached) {
            init();
        }
        return extendedWallpaperEffects;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean extendibleThemeManager() {
        if (!isCached) {
            init();
        }
        return extendibleThemeManager;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lockscreenCustomClocks() {
        if (!isCached) {
            init();
        }
        return lockscreenCustomClocks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newCustomizationPickerUi() {
        if (!isCached) {
            init();
        }
        return newCustomizationPickerUi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newTouchpadGesturesTutorial() {
        if (!isCached) {
            init();
        }
        return newTouchpadGesturesTutorial;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationDotContrastBorder() {
        if (!isCached) {
            init();
        }
        return notificationDotContrastBorder;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean panAndZoomInExtendedWallpaperEffects() {
        if (!isCached) {
            init();
        }
        return panAndZoomInExtendedWallpaperEffects;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotContextUrl() {
        if (!isCached) {
            init();
        }
        return screenshotContextUrl;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeAllowBackGesture() {
        if (!isCached) {
            init();
        }
        return shadeAllowBackGesture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sidefpsControllerRefactor() {
        if (!isCached) {
            init();
        }
        return sidefpsControllerRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceAqiUpdatedDesign() {
        if (!isCached) {
            init();
        }
        return smartspaceAqiUpdatedDesign;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceSemanticWeatherData() {
        if (!isCached) {
            init();
        }
        return smartspaceSemanticWeatherData;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceSportsCardBackground() {
        if (!isCached) {
            init();
        }
        return smartspaceSportsCardBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceUiUpdate() {
        if (!isCached) {
            init();
        }
        return smartspaceUiUpdate;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceUiUpdateResources() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceWeatherUseMonochromeFontIcons() {
        if (!isCached) {
            init();
        }
        return smartspaceWeatherUseMonochromeFontIcons;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return statusBarConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean threeButtonCornerSwipe() {
        if (!isCached) {
            init();
        }
        return threeButtonCornerSwipe;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean usePreferredImageEditor() {
        if (!isCached) {
            init();
        }
        return usePreferredImageEditor;
    }

}
