package com.android.launcher3;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean accessibilityScrollOnAllapps = true;
    private static boolean allAppsBlur = true;
    private static boolean allAppsSheetForHandheld = true;
    private static boolean avoidDisplayCutoutBubbleBar = true;
    private static boolean blurredHomeAnimation = false;
    private static boolean clearScrimOnReset = true;
    private static boolean enableAllAppsButtonInHotseat = false;
    private static boolean enableAltTabKqsFlatenning = true;
    private static boolean enableAltTabKqsOnConnectedDisplays = true;
    private static boolean enableAppWidgetPickerRefactor = false;
    private static boolean enableAutoStashConnectedDisplayTaskbar = true;
    private static boolean enableCategorizedWidgetSuggestions = true;
    private static boolean enableContrastTiles = true;
    private static boolean enableCursorDrivenWorkflows = false;
    private static boolean enableCustomHeightForAllAppsOnCd = true;
    private static boolean enableDesktopExplodedView = true;
    private static boolean enableExpandingPauseWorkButton = true;
    private static boolean enableExpressiveDismissTaskMotion = true;
    private static boolean enableExpressiveFolderExpansion = false;
    private static boolean enableFallbackOverviewInWindow = false;
    private static boolean enableFocusOutline = true;
    private static boolean enableGestureNavOnConnectedDisplays = true;
    private static boolean enableGridOnlyOverview = true;
    private static boolean enableGrowthNudge = false;
    private static boolean enableHomeTransitionListener = true;
    private static boolean enableLaterIsLockedCheck = false;
    private static boolean enableLauncherIconShapes = true;
    private static boolean enableLauncherOverviewInWindow = false;
    private static boolean enableLauncherVisualRefresh = true;
    private static boolean enableMetaTabToggleInOverview = true;
    private static boolean enableMouseInteractionChanges = true;
    private static boolean enableMultiInstanceMenuTaskbar = true;
    private static boolean enableNewAllSetAnimation = true;
    private static boolean enableOverviewBackgroundWallpaperBlur = true;
    private static boolean enableOverviewDesktopTileWallpaperBackground = false;
    private static boolean enableOverviewIconMenu = true;
    private static boolean enableOverviewOnConnectedDisplays = true;
    private static boolean enablePredictiveBackInOverview = false;
    private static boolean enablePreventOverviewMouseDrag = false;
    private static boolean enablePrivateSpace = true;
    private static boolean enableQsbOnHotseat = true;
    private static boolean enableRebootUnlockAnimation = false;
    private static boolean enableRecentsInTaskbar = false;
    private static boolean enableRecentsWindowProtoLog = true;
    private static boolean enableRefactorDigitalWellbeingToast = true;
    private static boolean enableRefactorTaskContentView = true;
    private static boolean enableRefactorTaskThumbnail = true;
    private static boolean enableResponsiveWorkspace = true;
    private static boolean enableReversibleHomeActionCorner = false;
    private static boolean enableScalabilityForDesktopExperience = true;
    private static boolean enableSimultaneousOverviewTriggerOnExtendedDesktop = false;
    private static boolean enableStateManagerProtoLog = true;
    private static boolean enableStrictMode = false;
    private static boolean enableSupportForArchiving = true;
    private static boolean enableSystemDrag = false;
    private static boolean enableTabletTwoPanePickerV2 = false;
    private static boolean enableTaskbarBehindShade = false;
    private static boolean enableTaskbarCustomization = true;
    private static boolean enableTaskbarDragAndDrop = false;
    private static boolean enableTaskbarForDirectBoot = true;
    private static boolean enableTaskbarIconContainer = true;
    private static boolean enableTaskbarNoRecreate = false;
    private static boolean enableTaskbarPinning = true;
    private static boolean enableTaskbarRecentsThemedIcons = true;
    private static boolean enableTaskbarUiThread = false;
    private static boolean enableTieredWidgetsByDefaultInPicker = false;
    private static boolean enableTwoPaneLauncherSettings = false;
    private static boolean enableTwolineAllapps = false;
    private static boolean enableTwolineToggle = true;
    private static boolean enableUnfoldStateAnimation = false;
    private static boolean enableWidgetPickerRefactor = true;
    private static boolean enabledFoldersInAllApps = false;
    private static boolean expandableLongPressMenu = false;
    private static boolean firstPagePinnedWidgetRemovalToggle = false;
    private static boolean floatingSearchBar = false;
    private static boolean forceMonochromeAppIcons = true;
    private static boolean forceMonochromeAppIconsAdaptColors = false;
    private static boolean homeScreenEditImprovements = false;
    private static boolean ignoreThreeFingerTrackpadForNavHandleLongPress = true;
    private static boolean injectableModelItems = true;
    private static boolean letterFastScroller = false;
    private static boolean modelRepository = true;
    private static boolean msdlFeedback = true;
    private static boolean nudgePill = false;
    private static boolean oneGridMountedMode = false;
    private static boolean oneGridRotationHandling = false;
    private static boolean oneGridSpecs = true;
    private static boolean privateSpaceAddFloatingMaskView = true;
    private static boolean privateSpaceAnimation = true;
    private static boolean privateSpaceRestrictAccessibilityDrag = true;
    private static boolean privateSpaceRestrictItemDrag = true;
    private static boolean privateSpaceSysAppsSeparation = true;
    private static boolean refactorTaskbarUiState = true;
    private static boolean removeAppsRefreshOnRightClick = true;
    private static boolean restoreArchivedAppIconsFromDb = true;
    private static boolean restoreArchivedShortcuts = true;
    private static boolean showCloseButtonOnTaskviewHover = true;
    private static boolean showFilesOnHomeScreen = false;
    private static boolean showTaskbarPinningPopupFromAnywhere = true;
    private static boolean simplifiedLauncherModelBinding = true;
    private static boolean syncAppLaunchWithTaskbarStash = true;
    private static boolean taskbarQuietModeChangeSupport = false;
    private static boolean unpinFirstPagePinnedWidget = false;
    private static boolean useNewIconForArchivedApps = true;
    private static boolean useSystemRadiusForAppWidgets = true;
    private static boolean workSchedulerInWorkProfile = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("com.android.launcher3", 0x6184856FACB493A5L);
            enableGrowthNudge = reader.getBooleanFlagValue(24);
            accessibilityScrollOnAllapps = reader.getBooleanFlagValue(0);
            // The standalone APK must match Launcher3's Soong product defaults.
            // A device-side aconfig store may omit this package (or expose an
            // older checksum), which otherwise silently selects the opaque
            // legacy all-apps sheet even though the device supports blur.
            allAppsBlur = true;
            allAppsSheetForHandheld = true;
            blurredHomeAnimation = reader.getBooleanFlagValue(4);
            enableAllAppsButtonInHotseat = reader.getBooleanFlagValue(6);
            enableAppWidgetPickerRefactor = reader.getBooleanFlagValue(9);
            enableAutoStashConnectedDisplayTaskbar = reader.getBooleanFlagValue(10);
            enableCategorizedWidgetSuggestions = reader.getBooleanFlagValue(11);
            enableContrastTiles = reader.getBooleanFlagValue(12);
            enableCursorDrivenWorkflows = reader.getBooleanFlagValue(13);
            enableCustomHeightForAllAppsOnCd = reader.getBooleanFlagValue(14);
            enableExpandingPauseWorkButton = reader.getBooleanFlagValue(16);
            enableExpressiveFolderExpansion = reader.getBooleanFlagValue(18);
            enableFocusOutline = reader.getBooleanFlagValue(21);
            enableHomeTransitionListener = reader.getBooleanFlagValue(25);
            enableLauncherIconShapes = reader.getBooleanFlagValue(28);
            enableLauncherVisualRefresh = reader.getBooleanFlagValue(30);
            enableMouseInteractionChanges = reader.getBooleanFlagValue(32);
            enableMultiInstanceMenuTaskbar = reader.getBooleanFlagValue(33);
            enableNewAllSetAnimation = reader.getBooleanFlagValue(35);
            enableRebootUnlockAnimation = reader.getBooleanFlagValue(44);
            enableRecentsInTaskbar = reader.getBooleanFlagValue(45);
            enableResponsiveWorkspace = reader.getBooleanFlagValue(50);
            enableReversibleHomeActionCorner = reader.getBooleanFlagValue(51);
            enableScalabilityForDesktopExperience = reader.getBooleanFlagValue(52);
            enableStrictMode = reader.getBooleanFlagValue(55);
            enableSupportForArchiving = reader.getBooleanFlagValue(56);
            enableSystemDrag = reader.getBooleanFlagValue(57);
            enableTabletTwoPanePickerV2 = reader.getBooleanFlagValue(58);
            enableTaskbarCustomization = reader.getBooleanFlagValue(60);
            enableTaskbarDragAndDrop = reader.getBooleanFlagValue(61);
            enableTaskbarForDirectBoot = reader.getBooleanFlagValue(62);
            enableTaskbarIconContainer = reader.getBooleanFlagValue(63);
            enableTaskbarNoRecreate = reader.getBooleanFlagValue(64);
            enableTaskbarPinning = reader.getBooleanFlagValue(65);
            enableTaskbarRecentsThemedIcons = reader.getBooleanFlagValue(66);
            enableTaskbarUiThread = reader.getBooleanFlagValue(67);
            enableTieredWidgetsByDefaultInPicker = reader.getBooleanFlagValue(68);
            enableTwoPaneLauncherSettings = reader.getBooleanFlagValue(69);
            enableTwolineAllapps = reader.getBooleanFlagValue(70);
            enableTwolineToggle = reader.getBooleanFlagValue(71);
            enableUnfoldStateAnimation = reader.getBooleanFlagValue(72);
            enableWidgetPickerRefactor = reader.getBooleanFlagValue(73);
            enabledFoldersInAllApps = reader.getBooleanFlagValue(74);
            expandableLongPressMenu = reader.getBooleanFlagValue(75);
            firstPagePinnedWidgetRemovalToggle = reader.getBooleanFlagValue(76);
            floatingSearchBar = reader.getBooleanFlagValue(77);
            forceMonochromeAppIcons = reader.getBooleanFlagValue(78);
            forceMonochromeAppIconsAdaptColors = reader.getBooleanFlagValue(79);
            homeScreenEditImprovements = reader.getBooleanFlagValue(80);
            // Match the stock LineageOS Soong product default: home_screen_edit_improvements
            // is disabled there, so the widget resize frame appears only after the drop
            // (release), not while long-pressing. Without this, a device whose aconfig
            // store omits this package (or exposes a stale checksum) keeps the legacy
            // true default and the frame pops up on long-press.
            homeScreenEditImprovements = false;
            ignoreThreeFingerTrackpadForNavHandleLongPress = reader.getBooleanFlagValue(81);
            injectableModelItems = reader.getBooleanFlagValue(82);
            letterFastScroller = reader.getBooleanFlagValue(83);
            modelRepository = reader.getBooleanFlagValue(84);
            msdlFeedback = reader.getBooleanFlagValue(85);
            oneGridMountedMode = reader.getBooleanFlagValue(87);
            oneGridRotationHandling = reader.getBooleanFlagValue(88);
            oneGridSpecs = reader.getBooleanFlagValue(89);
            refactorTaskbarUiState = reader.getBooleanFlagValue(95);
            removeAppsRefreshOnRightClick = reader.getBooleanFlagValue(96);
            restoreArchivedAppIconsFromDb = reader.getBooleanFlagValue(97);
            restoreArchivedShortcuts = reader.getBooleanFlagValue(98);
            showFilesOnHomeScreen = reader.getBooleanFlagValue(100);
            showTaskbarPinningPopupFromAnywhere = reader.getBooleanFlagValue(101);
            simplifiedLauncherModelBinding = reader.getBooleanFlagValue(102);
            syncAppLaunchWithTaskbarStash = reader.getBooleanFlagValue(103);
            taskbarQuietModeChangeSupport = reader.getBooleanFlagValue(104);
            unpinFirstPagePinnedWidget = reader.getBooleanFlagValue(105);
            useNewIconForArchivedApps = reader.getBooleanFlagValue(106);
            useSystemRadiusForAppWidgets = reader.getBooleanFlagValue(107);
            workSchedulerInWorkProfile = reader.getBooleanFlagValue(108);
            enableDesktopExplodedView = reader.getBooleanFlagValue(15);
            enableExpressiveDismissTaskMotion = reader.getBooleanFlagValue(17);
            enableGridOnlyOverview = reader.getBooleanFlagValue(23);
            enableLaterIsLockedCheck = reader.getBooleanFlagValue(26);
            enableMetaTabToggleInOverview = reader.getBooleanFlagValue(31);
            enableOverviewBackgroundWallpaperBlur = reader.getBooleanFlagValue(36);
            enableOverviewDesktopTileWallpaperBackground = reader.getBooleanFlagValue(37);
            enableOverviewIconMenu = reader.getBooleanFlagValue(38);
            enableOverviewOnConnectedDisplays = reader.getBooleanFlagValue(39);
            enablePredictiveBackInOverview = reader.getBooleanFlagValue(40);
            enablePreventOverviewMouseDrag = reader.getBooleanFlagValue(41);
            enableRefactorDigitalWellbeingToast = reader.getBooleanFlagValue(47);
            enableRefactorTaskContentView = reader.getBooleanFlagValue(48);
            enableRefactorTaskThumbnail = reader.getBooleanFlagValue(49);
            enableSimultaneousOverviewTriggerOnExtendedDesktop = reader.getBooleanFlagValue(53);
            showCloseButtonOnTaskviewHover = reader.getBooleanFlagValue(99);
            clearScrimOnReset = reader.getBooleanFlagValue(5);
            enablePrivateSpace = reader.getBooleanFlagValue(42);
            enableQsbOnHotseat = reader.getBooleanFlagValue(43);
            nudgePill = reader.getBooleanFlagValue(86);
            privateSpaceAddFloatingMaskView = reader.getBooleanFlagValue(90);
            privateSpaceAnimation = reader.getBooleanFlagValue(91);
            privateSpaceRestrictAccessibilityDrag = reader.getBooleanFlagValue(92);
            privateSpaceRestrictItemDrag = reader.getBooleanFlagValue(93);
            privateSpaceSysAppsSeparation = reader.getBooleanFlagValue(94);
            enableAltTabKqsFlatenning = reader.getBooleanFlagValue(7);
            enableAltTabKqsOnConnectedDisplays = reader.getBooleanFlagValue(8);
            enableFallbackOverviewInWindow = reader.getBooleanFlagValue(19);
            enableGestureNavOnConnectedDisplays = reader.getBooleanFlagValue(22);
            enableLauncherOverviewInWindow = reader.getBooleanFlagValue(29);
            enableRecentsWindowProtoLog = reader.getBooleanFlagValue(46);
            enableStateManagerProtoLog = reader.getBooleanFlagValue(54);
            enableTaskbarBehindShade = reader.getBooleanFlagValue(59);
            avoidDisplayCutoutBubbleBar = reader.getBooleanFlagValue(3);
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

    public boolean accessibilityScrollOnAllapps() {
        if (!isCached) {
            init();
        }
        return accessibilityScrollOnAllapps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allAppsBlur() {
        if (!isCached) {
            init();
        }
        return allAppsBlur;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allAppsSheetForHandheld() {
        if (!isCached) {
            init();
        }
        return allAppsSheetForHandheld;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean avoidDisplayCutoutBubbleBar() {
        if (!isCached) {
            init();
        }
        return avoidDisplayCutoutBubbleBar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean blurredHomeAnimation() {
        if (!isCached) {
            init();
        }
        return blurredHomeAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clearScrimOnReset() {
        if (!isCached) {
            init();
        }
        return clearScrimOnReset;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAllAppsButtonInHotseat() {
        if (!isCached) {
            init();
        }
        return enableAllAppsButtonInHotseat;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAltTabKqsFlatenning() {
        if (!isCached) {
            init();
        }
        return enableAltTabKqsFlatenning;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAltTabKqsOnConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableAltTabKqsOnConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAppWidgetPickerRefactor() {
        if (!isCached) {
            init();
        }
        return enableAppWidgetPickerRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAutoStashConnectedDisplayTaskbar() {
        if (!isCached) {
            init();
        }
        return enableAutoStashConnectedDisplayTaskbar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCategorizedWidgetSuggestions() {
        if (!isCached) {
            init();
        }
        return enableCategorizedWidgetSuggestions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableContrastTiles() {
        if (!isCached) {
            init();
        }
        return enableContrastTiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCursorDrivenWorkflows() {
        if (!isCached) {
            init();
        }
        return enableCursorDrivenWorkflows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCustomHeightForAllAppsOnCd() {
        if (!isCached) {
            init();
        }
        return enableCustomHeightForAllAppsOnCd;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopExplodedView() {
        if (!isCached) {
            init();
        }
        return enableDesktopExplodedView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableExpandingPauseWorkButton() {
        if (!isCached) {
            init();
        }
        return enableExpandingPauseWorkButton;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableExpressiveDismissTaskMotion() {
        if (!isCached) {
            init();
        }
        return enableExpressiveDismissTaskMotion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableExpressiveFolderExpansion() {
        if (!isCached) {
            init();
        }
        return enableExpressiveFolderExpansion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFallbackOverviewInWindow() {
        if (!isCached) {
            init();
        }
        return enableFallbackOverviewInWindow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFirstScreenBroadcastArchivingExtras() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFocusOutline() {
        if (!isCached) {
            init();
        }
        return enableFocusOutline;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableGestureNavOnConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableGestureNavOnConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableGridOnlyOverview() {
        if (!isCached) {
            init();
        }
        return enableGridOnlyOverview;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableGrowthNudge() {
        if (!isCached) {
            init();
        }
        return enableGrowthNudge;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHomeTransitionListener() {
        if (!isCached) {
            init();
        }
        return enableHomeTransitionListener;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLaterIsLockedCheck() {
        if (!isCached) {
            init();
        }
        return enableLaterIsLockedCheck;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLauncherBrMetricsFixed() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLauncherIconShapes() {
        if (!isCached) {
            init();
        }
        return enableLauncherIconShapes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLauncherOverviewInWindow() {
        if (!isCached) {
            init();
        }
        return enableLauncherOverviewInWindow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLauncherVisualRefresh() {
        if (!isCached) {
            init();
        }
        return enableLauncherVisualRefresh;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMetaTabToggleInOverview() {
        if (!isCached) {
            init();
        }
        return enableMetaTabToggleInOverview;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMouseInteractionChanges() {
        if (!isCached) {
            init();
        }
        return enableMouseInteractionChanges;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultiInstanceMenuTaskbar() {
        if (!isCached) {
            init();
        }
        return enableMultiInstanceMenuTaskbar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNarrowGridRestore() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNewAllSetAnimation() {
        if (!isCached) {
            init();
        }
        return enableNewAllSetAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOverviewBackgroundWallpaperBlur() {
        if (!isCached) {
            init();
        }
        return enableOverviewBackgroundWallpaperBlur;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOverviewDesktopTileWallpaperBackground() {
        if (!isCached) {
            init();
        }
        return enableOverviewDesktopTileWallpaperBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOverviewIconMenu() {
        if (!isCached) {
            init();
        }
        return enableOverviewIconMenu;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOverviewOnConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableOverviewOnConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePredictiveBackInOverview() {
        if (!isCached) {
            init();
        }
        return enablePredictiveBackInOverview;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePreventOverviewMouseDrag() {
        if (!isCached) {
            init();
        }
        return enablePreventOverviewMouseDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePrivateSpace() {
        if (!isCached) {
            init();
        }
        return enablePrivateSpace;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableQsbOnHotseat() {
        if (!isCached) {
            init();
        }
        return enableQsbOnHotseat;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRebootUnlockAnimation() {
        if (!isCached) {
            init();
        }
        return enableRebootUnlockAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRecentsInTaskbar() {
        if (!isCached) {
            init();
        }
        return enableRecentsInTaskbar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRecentsWindowProtoLog() {
        if (!isCached) {
            init();
        }
        return enableRecentsWindowProtoLog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRefactorDigitalWellbeingToast() {
        if (!isCached) {
            init();
        }
        return enableRefactorDigitalWellbeingToast;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRefactorTaskContentView() {
        if (!isCached) {
            init();
        }
        return enableRefactorTaskContentView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRefactorTaskThumbnail() {
        if (!isCached) {
            init();
        }
        return enableRefactorTaskThumbnail;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableResponsiveWorkspace() {
        if (!isCached) {
            init();
        }
        return enableResponsiveWorkspace;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableReversibleHomeActionCorner() {
        if (!isCached) {
            init();
        }
        return enableReversibleHomeActionCorner;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableScalabilityForDesktopExperience() {
        if (!isCached) {
            init();
        }
        return enableScalabilityForDesktopExperience;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSimultaneousOverviewTriggerOnExtendedDesktop() {
        if (!isCached) {
            init();
        }
        return enableSimultaneousOverviewTriggerOnExtendedDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableStateManagerProtoLog() {
        if (!isCached) {
            init();
        }
        return enableStateManagerProtoLog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableStrictMode() {
        if (!isCached) {
            init();
        }
        return enableStrictMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSupportForArchiving() {
        if (!isCached) {
            init();
        }
        return enableSupportForArchiving;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSystemDrag() {
        if (!isCached) {
            init();
        }
        return enableSystemDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTabletTwoPanePickerV2() {
        if (!isCached) {
            init();
        }
        return enableTabletTwoPanePickerV2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarBehindShade() {
        if (!isCached) {
            init();
        }
        return enableTaskbarBehindShade;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarCustomization() {
        if (!isCached) {
            init();
        }
        return enableTaskbarCustomization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarDragAndDrop() {
        if (!isCached) {
            init();
        }
        return enableTaskbarDragAndDrop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarForDirectBoot() {
        if (!isCached) {
            init();
        }
        return enableTaskbarForDirectBoot;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarIconContainer() {
        if (!isCached) {
            init();
        }
        return enableTaskbarIconContainer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarNoRecreate() {
        if (!isCached) {
            init();
        }
        return enableTaskbarNoRecreate;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarPinning() {
        if (!isCached) {
            init();
        }
        return enableTaskbarPinning;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarRecentsThemedIcons() {
        if (!isCached) {
            init();
        }
        return enableTaskbarRecentsThemedIcons;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarUiThread() {
        if (!isCached) {
            init();
        }
        return enableTaskbarUiThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTieredWidgetsByDefaultInPicker() {
        if (!isCached) {
            init();
        }
        return enableTieredWidgetsByDefaultInPicker;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTwoPaneLauncherSettings() {
        if (!isCached) {
            init();
        }
        return enableTwoPaneLauncherSettings;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTwolineAllapps() {
        if (!isCached) {
            init();
        }
        return enableTwolineAllapps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTwolineToggle() {
        if (!isCached) {
            init();
        }
        return enableTwolineToggle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableUnfoldStateAnimation() {
        if (!isCached) {
            init();
        }
        return enableUnfoldStateAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWidgetPickerRefactor() {
        if (!isCached) {
            init();
        }
        return enableWidgetPickerRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enabledFoldersInAllApps() {
        if (!isCached) {
            init();
        }
        return enabledFoldersInAllApps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean expandableLongPressMenu() {
        if (!isCached) {
            init();
        }
        return expandableLongPressMenu;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean firstPagePinnedWidgetRemovalToggle() {
        if (!isCached) {
            init();
        }
        return firstPagePinnedWidgetRemovalToggle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean floatingSearchBar() {
        if (!isCached) {
            init();
        }
        return floatingSearchBar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean forceMonochromeAppIcons() {
        if (!isCached) {
            init();
        }
        return forceMonochromeAppIcons;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean forceMonochromeAppIconsAdaptColors() {
        if (!isCached) {
            init();
        }
        return forceMonochromeAppIconsAdaptColors;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean homeScreenEditImprovements() {
        if (!isCached) {
            init();
        }
        return homeScreenEditImprovements;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ignoreThreeFingerTrackpadForNavHandleLongPress() {
        if (!isCached) {
            init();
        }
        return ignoreThreeFingerTrackpadForNavHandleLongPress;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean injectableModelItems() {
        if (!isCached) {
            init();
        }
        return injectableModelItems;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean letterFastScroller() {
        if (!isCached) {
            init();
        }
        return letterFastScroller;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean modelRepository() {
        if (!isCached) {
            init();
        }
        return modelRepository;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean msdlFeedback() {
        if (!isCached) {
            init();
        }
        return msdlFeedback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean nudgePill() {
        if (!isCached) {
            init();
        }
        return nudgePill;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean oneGridMountedMode() {
        if (!isCached) {
            init();
        }
        return oneGridMountedMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean oneGridRotationHandling() {
        if (!isCached) {
            init();
        }
        return oneGridRotationHandling;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean oneGridSpecs() {
        if (!isCached) {
            init();
        }
        return oneGridSpecs;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceAddFloatingMaskView() {
        if (!isCached) {
            init();
        }
        return privateSpaceAddFloatingMaskView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceAnimation() {
        if (!isCached) {
            init();
        }
        return privateSpaceAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceRestrictAccessibilityDrag() {
        if (!isCached) {
            init();
        }
        return privateSpaceRestrictAccessibilityDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceRestrictItemDrag() {
        if (!isCached) {
            init();
        }
        return privateSpaceRestrictItemDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceSysAppsSeparation() {
        if (!isCached) {
            init();
        }
        return privateSpaceSysAppsSeparation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean refactorTaskbarUiState() {
        if (!isCached) {
            init();
        }
        return refactorTaskbarUiState;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeAppsRefreshOnRightClick() {
        if (!isCached) {
            init();
        }
        return removeAppsRefreshOnRightClick;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restoreArchivedAppIconsFromDb() {
        if (!isCached) {
            init();
        }
        return restoreArchivedAppIconsFromDb;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restoreArchivedShortcuts() {
        if (!isCached) {
            init();
        }
        return restoreArchivedShortcuts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showCloseButtonOnTaskviewHover() {
        if (!isCached) {
            init();
        }
        return showCloseButtonOnTaskviewHover;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showFilesOnHomeScreen() {
        if (!isCached) {
            init();
        }
        return showFilesOnHomeScreen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showTaskbarPinningPopupFromAnywhere() {
        if (!isCached) {
            init();
        }
        return showTaskbarPinningPopupFromAnywhere;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean simplifiedLauncherModelBinding() {
        if (!isCached) {
            init();
        }
        return simplifiedLauncherModelBinding;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean syncAppLaunchWithTaskbarStash() {
        if (!isCached) {
            init();
        }
        return syncAppLaunchWithTaskbarStash;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean taskbarQuietModeChangeSupport() {
        if (!isCached) {
            init();
        }
        return taskbarQuietModeChangeSupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean unpinFirstPagePinnedWidget() {
        if (!isCached) {
            init();
        }
        return unpinFirstPagePinnedWidget;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useNewIconForArchivedApps() {
        if (!isCached) {
            init();
        }
        return useNewIconForArchivedApps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useSystemRadiusForAppWidgets() {
        if (!isCached) {
            init();
        }
        return useSystemRadiusForAppWidgets;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean workSchedulerInWorkProfile() {
        if (!isCached) {
            init();
        }
        return workSchedulerInWorkProfile;
    }

}
