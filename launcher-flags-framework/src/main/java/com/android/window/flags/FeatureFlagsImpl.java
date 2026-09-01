package com.android.window.flags;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean activityEmbeddingAbortCrossUidLaunchInFinishingTaskFragment = true;
    private static boolean activityEmbeddingDelayTaskFragmentFinishForActivityLaunch = true;
    private static boolean activityEmbeddingInteractiveDividerFlag = true;
    private static boolean activityEmbeddingMetrics = false;
    private static boolean alwaysDrawMagnificationFullscreenBorder = true;
    private static boolean alwaysSeqIdLayout = true;
    private static boolean alwaysSeqIdLayoutWear = true;
    private static boolean alwaysUpdateWallpaperPermission = true;
    private static boolean aodTransition = true;
    private static boolean appCompatRefactoringFixMultiwindowTaskHierarchy = true;
    private static boolean appCompatRefactoringForceChangeForLetterboxTransitions = false;
    private static boolean appCompatRefactoringRoundedCorners = true;
    private static boolean appCompatRefactoringRoundedCornersAnimation = false;
    private static boolean appCompatRefactoringSetAppboundsToNullWhenEmpty = true;
    private static boolean appCompatRefactoringSkipStartingWindowLetterbox = false;
    private static boolean appCompatRefactoringUseActivityLeashForLetterboxing = false;
    private static boolean appHandleNoRelayoutOnExclusionChange = true;
    private static boolean applyDeskActivationOnUserSwitch = true;
    private static boolean applyLifecycleOnPipChange = false;
    private static boolean avoidRebindingIntentionallyDisconnectedWallpaper = true;
    private static boolean balAdditionalStartModes = true;
    private static boolean balCheckBroadcastWhenDispatched = true;
    private static boolean balCoverIntentSender = true;
    private static boolean balDontBringExistingBackgroundTaskStackToFg = true;
    private static boolean balReduceGracePeriod = true;
    private static boolean balReportAbortedActivityStarts = true;
    private static boolean balRespectAppSwitchStateWhenCheckBoundByForegroundUid = true;
    private static boolean balSendIntentWithOptions = true;
    private static boolean cameraCompatFullscreenPickSameTaskActivity = true;
    private static boolean cameraCompatLandscapeCameraSupport = false;
    private static boolean cameraCompatUnifyCameraPolicies = true;
    private static boolean clearReusableScvhOnRelease = true;
    private static boolean closeFullscreenAndSplitscreenKeyboardShortcut = true;
    private static boolean closeTaskKeyboardShortcut = true;
    private static boolean closeToSquareConfigIncludesStatusBar = false;
    private static boolean currentAnimatorScaleUsesSharedMemory = false;
    private static boolean defaultDeskWithoutWarmupMigration = false;
    private static boolean deferResumeFocusInNonFocusedWindow = true;
    private static boolean deprecateSurfaceAnimationFrameCallback = false;
    private static boolean deprecateWindowAnimatorFrameCallback = false;
    private static boolean dimmingWallpaperForMaximizedAndTiled = false;
    private static boolean disableDesktopLaunchParamsOutsideDesktopBugFix = true;
    private static boolean disableNonResizableAppSnapResizing = true;
    private static boolean disableRestoreNonFullscreenBoundsOnConfigurationChange = false;
    private static boolean enableAccessibleCustomHeaders = true;
    private static boolean enableActivityEmbeddingSupportForConnectedDisplays = true;
    private static boolean enableAppHandlePositionReporting = true;
    private static boolean enableAppHeaderWithTaskDensity = true;
    private static boolean enableAppToWebEducationAnimation = true;
    private static boolean enableAutoRestartOnDisplayMove = true;
    private static boolean enableBlockNonDesktopDisplayWindowDragBugfix = true;
    private static boolean enableBoundsRestoringOnTilingExit = false;
    private static boolean enableBugFixesForSecondaryDisplay = true;
    private static boolean enableCameraCompatCompatibilityInfoRotateAndCropBugfix = true;
    private static boolean enableCameraCompatExternalDisplayRotationBugfix = true;
    private static boolean enableCameraCompatForDesktopWindowing = true;
    private static boolean enableCameraCompatForDesktopWindowingOptOut = true;
    private static boolean enableCameraCompatForDesktopWindowingOptOutApi = true;
    private static boolean enableCameraCompatSandboxDisplayRotationOnExternalDisplaysBugfix = true;
    private static boolean enableCameraCompatTrackTaskAndAppBugfix = true;
    private static boolean enableCaptionCompatInsetConversion = false;
    private static boolean enableCaptionCompatInsetForceConsumption = true;
    private static boolean enableCaptionCompatInsetForceConsumptionAlways = true;
    private static boolean enableCascadingWindows = true;
    private static boolean enableCloseLidInteraction = false;
    private static boolean enableCompatUiDesktopModeSynchronizationBugfix = true;
    private static boolean enableCompatUiVisibilityStatus = true;
    private static boolean enableCompatuiSysuiLauncherFix = true;
    private static boolean enableConnectedDisplaysDnd = true;
    private static boolean enableConnectedDisplaysPip = true;
    private static boolean enableConnectedDisplaysWallpaperPresentations = true;
    private static boolean enableConnectedDisplaysWindowDrag = true;
    private static boolean enableCrashLoggingForDesktop = true;
    private static boolean enableCrossDisplaysAppLaunchTransition = true;
    private static boolean enableCrossDisplaysPipTaskLaunch = true;
    private static boolean enableDesktopAppHandleAnimation = true;
    private static boolean enableDesktopAppHeaderStateChangeAnnouncements = true;
    private static boolean enableDesktopAppLaunchAlttabTransitions = true;
    private static boolean enableDesktopAppLaunchAlttabTransitionsBugfix = true;
    private static boolean enableDesktopAppLaunchBugfix = true;
    private static boolean enableDesktopAppLaunchTransitions = true;
    private static boolean enableDesktopAppLaunchTransitionsBugfix = true;
    private static boolean enableDesktopCloseShortcutBugfix = false;
    private static boolean enableDesktopCloseTaskAnimationInDtcBugfix = true;
    private static boolean enableDesktopFirstBasedDefaultToDesktopBugfix = true;
    private static boolean enableDesktopFirstBasedDragToMaximize = true;
    private static boolean enableDesktopFirstFullscreenRefocusBugfix = true;
    private static boolean enableDesktopFirstListener = true;
    private static boolean enableDesktopFirstPolicyInLpm = true;
    private static boolean enableDesktopFirstTopFullscreenBugfix = true;
    private static boolean enableDesktopImeBugfix = true;
    private static boolean enableDesktopImmersiveDragBugfix = true;
    private static boolean enableDesktopIndicatorInSeparateThreadBugfix = true;
    private static boolean enableDesktopInvisibleTaskRemovalCleanupBugfix = true;
    private static boolean enableDesktopModeThroughDevOption = true;
    private static boolean enableDesktopOpeningDeeplinkMinimizeAnimationBugfix = true;
    private static boolean enableDesktopRecentsTransitionsCornersBugfix = true;
    private static boolean enableDesktopSplitscreenTransitionBugfix = true;
    private static boolean enableDesktopSystemDialogsTransitions = true;
    private static boolean enableDesktopTabTearingLaunchAnimation = true;
    private static boolean enableDesktopTabTearingMinimizeAnimationBugfix = true;
    private static boolean enableDesktopTaskLimitSeparateTransition = true;
    private static boolean enableDesktopTaskbarOnFreeformDisplays = true;
    private static boolean enableDesktopTrampolineCloseAnimationBugfix = true;
    private static boolean enableDesktopWallpaperActivityForSystemUser = true;
    private static boolean enableDesktopWindowingAppHandleEducation = true;
    private static boolean enableDesktopWindowingAppToWeb = true;
    private static boolean enableDesktopWindowingAppToWebEducation = true;
    private static boolean enableDesktopWindowingAppToWebEducationIntegration = true;
    private static boolean enableDesktopWindowingBackNavigation = true;
    private static boolean enableDesktopWindowingEnterTransitionBugfix = true;
    private static boolean enableDesktopWindowingEnterTransitions = true;
    private static boolean enableDesktopWindowingEnterpriseBugfix = true;
    private static boolean enableDesktopWindowingExitByMinimizeTransitionBugfix = true;
    private static boolean enableDesktopWindowingExitTransitions = true;
    private static boolean enableDesktopWindowingExitTransitionsBugfix = true;
    private static boolean enableDesktopWindowingHsum = true;
    private static boolean enableDesktopWindowingImmersiveHandleHiding = true;
    private static boolean enableDesktopWindowingModalsPolicy = true;
    private static boolean enableDesktopWindowingMode = true;
    private static boolean enableDesktopWindowingMultiInstanceFeatures = true;
    private static boolean enableDesktopWindowingPersistence = true;
    private static boolean enableDesktopWindowingPip = true;
    private static boolean enableDesktopWindowingPipInOverviewBugfix = true;
    private static boolean enableDesktopWindowingQuickSwitch = true;
    private static boolean enableDesktopWindowingScvhCacheBugFix = true;
    private static boolean enableDesktopWindowingSizeConstraints = true;
    private static boolean enableDesktopWindowingTaskLimit = true;
    private static boolean enableDesktopWindowingTaskbarRunningApps = true;
    private static boolean enableDesktopWindowingTransitions = true;
    private static boolean enableDesktopWindowingWallpaperActivity = true;
    private static boolean enableDeviceStateAutoRotateSettingLogging = true;
    private static boolean enableDeviceStateAutoRotateSettingRefactor = true;
    private static boolean enableDialogDisplayFixes = true;
    private static boolean enableDisplayCompatMode = true;
    private static boolean enableDisplayDisconnectInteraction = true;
    private static boolean enableDisplayFocusInShellTransitions = true;
    private static boolean enableDisplayReconnectInteraction = true;
    private static boolean enableDisplayWindowingModeSwitching = true;
    private static boolean enableDragEndStableBoundsReset = true;
    private static boolean enableDragResizeSetUpInBgThread = true;
    private static boolean enableDragToDesktopIncomingTransitionsBugfix = true;
    private static boolean enableDragToMaximize = true;
    private static boolean enableDraggingPipAcrossDisplays = true;
    private static boolean enableDrawingAppHandle = true;
    private static boolean enableDreamActivityWindowingExclusion = true;
    private static boolean enableDynamicRadiusComputationBugfix = true;
    private static boolean enableEmptyDeskOnMinimize = true;
    private static boolean enableExperimentalBubblesController = true;
    private static boolean enableExternalDisplayPersistenceBugfix = true;
    private static boolean enableFreeformBoxShadows = true;
    private static boolean enableFreeformDisplayLaunchParams = true;
    private static boolean enableFullScreenWindowOnRemovingSplitScreenStageBugfix = true;
    private static boolean enableFullscreenWindowControls = true;
    private static boolean enableFullyImmersiveInDesktop = true;
    private static boolean enableHandleInputFix = true;
    private static boolean enableHandlersDebuggingMode = false;
    private static boolean enableHoldToDragAppHandle = true;
    private static boolean enableIndependentBackInProjected = true;
    private static boolean enableInorderTransitionCallbacksForDesktop = true;
    private static boolean enableInputLayerTransitionFix = true;
    private static boolean enableInteractionDependentTabTearingBounds = false;
    private static boolean enableInteractivePictureInPicture = false;
    private static boolean enableKeyGestureHandlerForSysui = true;
    private static boolean enableLauncherHandleGoHomeKeyboardShortcut = true;
    private static boolean enableMinimizeButton = true;
    private static boolean enableMirrorDisplayNoActivity = true;
    private static boolean enableModalsFullscreenWithPermission = true;
    private static boolean enableModalsFullscreenWithPlatformSignature = true;
    private static boolean enableMoveToNextDisplayShortcut = true;
    private static boolean enableMultiDisplayHomeFocusBugFix = false;
    private static boolean enableMultiDisplaySplit = false;
    private static boolean enableMultidisplayTrackpadBackGesture = true;
    private static boolean enableMultipleDesktopsBackend = true;
    private static boolean enableMultipleDesktopsDefaultActivationInDesktopFirstDisplays = false;
    private static boolean enableMultipleDesktopsFrontend = true;
    private static boolean enableNoWindowDecorationForDesks = true;
    private static boolean enableNonDefaultDisplaySplit = false;
    private static boolean enableNonDefaultDisplaySplitBugfix = false;
    private static boolean enableOpaqueBackgroundForTransparentWindows = true;
    private static boolean enableOverflowButtonForTaskbarPinnedItems = false;
    private static boolean enablePerDisplayDesktopWallpaperActivity = true;
    private static boolean enablePerDisplayPackageContextCacheInStatusbarNotif = true;
    private static boolean enablePerDisplayWindowDecorViewHostPool = true;
    private static boolean enablePersistingDisplaySizeForConnectedDisplays = true;
    private static boolean enablePinningAppWithContextMenu = true;
    private static boolean enablePipParamsUpdateNotificationBugfix = true;
    private static boolean enablePresentationDisallowedOnUnfocusedHostTask = true;
    private static boolean enablePresentationForConnectedDisplays = true;
    private static boolean enableProjectedDisplayDesktopMode = true;
    private static boolean enableQuickswitchDesktopSplitBugfix = true;
    private static boolean enableRejectHomeTransition = true;
    private static boolean enableRemoveStatusBarInputLayer = true;
    private static boolean enableRequestFullscreenBugfix = true;
    private static boolean enableRequestFullscreenRefactor = false;
    private static boolean enableRequestFullscreenRestoreFreeformBugfix = true;
    private static boolean enableResizingMetrics = true;
    private static boolean enableRestartMenuForConnectedDisplays = true;
    private static boolean enableRestoreToPreviousSizeFromDesktopImmersive = true;
    private static boolean enableSeeThroughTaskFragments = true;
    private static boolean enableShellInitialBoundsRegressionBugFix = true;
    private static boolean enableShrinkWindowBoundsAfterDrag = true;
    private static boolean enableSizeCompatModeImprovementsForConnectedDisplays = true;
    private static boolean enableStartLaunchTransitionFromTaskbarBugfix = true;
    private static boolean enableSysDecorsCallbacksViaWm = true;
    private static boolean enableTallAppHeaders = true;
    private static boolean enableTaskResizingKeyboardShortcuts = true;
    private static boolean enableTaskStackObserverInShell = true;
    private static boolean enableTaskbarConnectedDisplays = true;
    private static boolean enableTaskbarOverflow = true;
    private static boolean enableTaskbarRecentTasksThrottleBugfix = true;
    private static boolean enableTaskbarRecentsLayoutTransition = true;
    private static boolean enableThemedAppHeaders = true;
    private static boolean enableTileResizing = true;
    private static boolean enableTransitionOnActivitySetRequestedOrientation = true;
    private static boolean enableUpdatedDisplayConnectionDialog = true;
    private static boolean enableUpscalingSizeCompatOnExitingDesktopBugfix = true;
    private static boolean enableVisualIndicatorInTransitionBugfix = true;
    private static boolean enableWindowDecorationRefactor = true;
    private static boolean enableWindowDropSmoothTransition = true;
    private static boolean enableWindowRepositioningApi = true;
    private static boolean enableWindowingDynamicInitialBounds = true;
    private static boolean enableWindowingEdgeDragResize = true;
    private static boolean enableWindowingScaledResizing = true;
    private static boolean enableWindowingTaskStackOrderBugfix = true;
    private static boolean enableWindowingTransitionHandlersObservers = true;
    private static boolean ensureWallpaperDrawnOnDisplaySwitch = false;
    private static boolean enterDesktopByDefaultOnFreeformDisplays = true;
    private static boolean excludeCaptionFromAppBounds = true;
    private static boolean excludeDeskRootsFromDesktopTasks = true;
    private static boolean excludingLayerFromTaskSnapshot = true;
    private static boolean fallbackTransitionPlayer = true;
    private static boolean fixBalReparentExistingTask = true;
    private static boolean fixBubbleTrampolineAnimation = false;
    private static boolean fixLeakingVisualIndicator = true;
    private static boolean fixRapidTopResumedSwitch = true;
    private static boolean forceCloseTopTransparentFullscreenTask = true;
    private static boolean formFactorBasedDesktopFirstSwitch = true;
    private static boolean handleIncompatibleTasksInDesktopLaunchParams = true;
    private static boolean ignoreAspectRatioRestrictionsForResizeableFreeformActivities = true;
    private static boolean ignoreCurrentParamsInDesktopLaunchParams = true;
    private static boolean ignoreOverrideTaskBoundsIfIncompatibleWithDisplay = true;
    private static boolean imeBackCallbackLeakPrevention = true;
    private static boolean includeTopTransparentFullscreenTaskInDesktopHeuristic = true;
    private static boolean inheritTaskBoundsForTrampolineTaskLaunches = true;
    private static boolean keyboardShortcutsToSwitchDesks = true;
    private static boolean keyguardRemoveDefaultDisplayUsage = true;
    private static boolean letterboxBackgroundWallpaper = false;
    private static boolean limitSystemFullscreenOverrideToDefaultDisplay = true;
    private static boolean migrateBasicLegacyReady = true;
    private static boolean moveToExternalDisplayShortcut = false;
    private static boolean moveToNextDisplayShortcutWithProjectedMode = true;
    private static boolean multiCrop = true;
    private static boolean multipleSystemNavigationObserverCallbacks = true;
    private static boolean nestedTasksWithIndependentBoundsBugfix = true;
    private static boolean noAlphaRotationEnterAnimation = true;
    private static boolean optOutOverrideOrientationToUser = false;
    private static boolean parallelCdTransitionsDuringRecents = true;
    private static boolean polishCloseWallpaperIncludesOpenChange = true;
    private static boolean portWindowSizeAnimation = true;
    private static boolean predictiveBackCallbackCancellationFix = true;
    private static boolean predictiveBackDelayWmTransition = true;
    private static boolean predictiveBackInterceptTransition = true;
    private static boolean predictiveBackStopKeycodeBackForwarding = false;
    private static boolean preserveRecentsTaskConfigurationOnRelaunch = true;
    private static boolean reenableAppHandleAnimations = true;
    private static boolean reenableAppHandleColorAnimations = false;
    private static boolean refactorMatchParentBounds = true;
    private static boolean removeDeskOnLastTaskRemoval = true;
    private static boolean removeGetDimmer = true;
    private static boolean repositoryBasedPersistence = true;
    private static boolean repositoryBasedPersistenceBgThread = false;
    private static boolean respectFullscreenActivityOptionInDesktopLaunchParams = true;
    private static boolean respectOrientationChangeForUnresizeable = true;
    private static boolean respectRequestedTaskSnapshotResolution = true;
    private static boolean restrictFreeformHiddenSystemBarsToFillingTasks = true;
    private static boolean safeRegionLetterboxingV1 = true;
    private static boolean scrollingFromLetterbox = false;
    private static boolean scvhSetFocusable = false;
    private static boolean showAppHandleLargeScreens = false;
    private static boolean showBiometricPromptSecondaryDisplayMessage = true;
    private static boolean showDesktopExperienceDevOption = true;
    private static boolean showDesktopWindowingDevOption = true;
    private static boolean showHomeBehindDesktop = true;
    private static boolean skipCompatUiEducationInDesktopMode = true;
    private static boolean skipDeactivationOfDeskWithNothingInFront = true;
    private static boolean skipDecorViewRelayoutWhenClosingBugfix = true;
    private static boolean systemContentPriority = true;
    private static boolean systemUiPostAnimationEnd = true;
    private static boolean taskbarRunningTasksInSplitscreenSelect = true;
    private static boolean toggleFullscreenStateViaFullscreenKey = true;
    private static boolean touchPassThroughOptIn = true;
    private static boolean transferStartingWindowToNextWhenInvisible = true;
    private static boolean transitReadyTracking = false;
    private static boolean unifyShellBinders = false;
    private static boolean universalResizableByDefault = true;
    private static boolean updateTaskCropInSync = true;
    private static boolean useInputReportedFocusForAccessibility = false;
    private static boolean vdmForceAppUniversalResizableApi = true;
    private static boolean waitForPresentFenceOnDisplaySwitch = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("com.android.window.flags", 0xEAE8DCA38737ECDAL);
            safeRegionLetterboxingV1 = reader.getBooleanFlagValue(306);
            alwaysDrawMagnificationFullscreenBorder = reader.getBooleanFlagValue(5);
            imeBackCallbackLeakPrevention = reader.getBooleanFlagValue(260);
            appCompatRefactoringFixMultiwindowTaskHierarchy = reader.getBooleanFlagValue(12);
            appCompatRefactoringForceChangeForLetterboxTransitions = reader.getBooleanFlagValue(13);
            appCompatRefactoringRoundedCorners = reader.getBooleanFlagValue(14);
            appCompatRefactoringRoundedCornersAnimation = reader.getBooleanFlagValue(15);
            appCompatRefactoringSetAppboundsToNullWhenEmpty = reader.getBooleanFlagValue(16);
            appCompatRefactoringSkipStartingWindowLetterbox = reader.getBooleanFlagValue(17);
            appCompatRefactoringUseActivityLeashForLetterboxing = reader.getBooleanFlagValue(18);
            cameraCompatFullscreenPickSameTaskActivity = reader.getBooleanFlagValue(33);
            cameraCompatLandscapeCameraSupport = reader.getBooleanFlagValue(34);
            cameraCompatUnifyCameraPolicies = reader.getBooleanFlagValue(35);
            enableCompatuiSysuiLauncherFix = reader.getBooleanFlagValue(78);
            enableTransitionOnActivitySetRequestedOrientation = reader.getBooleanFlagValue(226);
            ignoreAspectRatioRestrictionsForResizeableFreeformActivities = reader.getBooleanFlagValue(256);
            letterboxBackgroundWallpaper = reader.getBooleanFlagValue(266);
            moveToExternalDisplayShortcut = reader.getBooleanFlagValue(269);
            scrollingFromLetterbox = reader.getBooleanFlagValue(308);
            vdmForceAppUniversalResizableApi = reader.getBooleanFlagValue(338);
            applyDeskActivationOnUserSwitch = reader.getBooleanFlagValue(20);
            clearReusableScvhOnRelease = reader.getBooleanFlagValue(36);
            closeFullscreenAndSplitscreenKeyboardShortcut = reader.getBooleanFlagValue(37);
            closeTaskKeyboardShortcut = reader.getBooleanFlagValue(38);
            defaultDeskWithoutWarmupMigration = reader.getBooleanFlagValue(42);
            deferResumeFocusInNonFocusedWindow = reader.getBooleanFlagValue(43);
            dimmingWallpaperForMaximizedAndTiled = reader.getBooleanFlagValue(47);
            disableDesktopLaunchParamsOutsideDesktopBugFix = reader.getBooleanFlagValue(48);
            disableNonResizableAppSnapResizing = reader.getBooleanFlagValue(49);
            disableRestoreNonFullscreenBoundsOnConfigurationChange = reader.getBooleanFlagValue(50);
            enableAccessibleCustomHeaders = reader.getBooleanFlagValue(52);
            enableActivityEmbeddingSupportForConnectedDisplays = reader.getBooleanFlagValue(53);
            enableAppHandlePositionReporting = reader.getBooleanFlagValue(54);
            enableAppHeaderWithTaskDensity = reader.getBooleanFlagValue(55);
            enableAppToWebEducationAnimation = reader.getBooleanFlagValue(56);
            enableAutoRestartOnDisplayMove = reader.getBooleanFlagValue(57);
            enableBlockNonDesktopDisplayWindowDragBugfix = reader.getBooleanFlagValue(59);
            enableBoundsRestoringOnTilingExit = reader.getBooleanFlagValue(61);
            enableBugFixesForSecondaryDisplay = reader.getBooleanFlagValue(63);
            enableCameraCompatCompatibilityInfoRotateAndCropBugfix = reader.getBooleanFlagValue(64);
            enableCameraCompatExternalDisplayRotationBugfix = reader.getBooleanFlagValue(65);
            enableCameraCompatForDesktopWindowing = reader.getBooleanFlagValue(66);
            enableCameraCompatForDesktopWindowingOptOut = reader.getBooleanFlagValue(67);
            enableCameraCompatForDesktopWindowingOptOutApi = reader.getBooleanFlagValue(68);
            enableCameraCompatSandboxDisplayRotationOnExternalDisplaysBugfix = reader.getBooleanFlagValue(69);
            enableCameraCompatTrackTaskAndAppBugfix = reader.getBooleanFlagValue(70);
            enableCaptionCompatInsetConversion = reader.getBooleanFlagValue(71);
            enableCaptionCompatInsetForceConsumption = reader.getBooleanFlagValue(72);
            enableCaptionCompatInsetForceConsumptionAlways = reader.getBooleanFlagValue(73);
            enableCascadingWindows = reader.getBooleanFlagValue(74);
            enableCloseLidInteraction = reader.getBooleanFlagValue(75);
            enableCompatUiDesktopModeSynchronizationBugfix = reader.getBooleanFlagValue(76);
            enableCompatUiVisibilityStatus = reader.getBooleanFlagValue(77);
            enableConnectedDisplaysDnd = reader.getBooleanFlagValue(79);
            enableConnectedDisplaysPip = reader.getBooleanFlagValue(80);
            enableConnectedDisplaysWallpaperPresentations = reader.getBooleanFlagValue(81);
            enableConnectedDisplaysWindowDrag = reader.getBooleanFlagValue(82);
            enableCrashLoggingForDesktop = reader.getBooleanFlagValue(83);
            enableCrossDisplaysAppLaunchTransition = reader.getBooleanFlagValue(84);
            enableCrossDisplaysPipTaskLaunch = reader.getBooleanFlagValue(85);
            enableDesktopAppHandleAnimation = reader.getBooleanFlagValue(86);
            enableDesktopAppHeaderStateChangeAnnouncements = reader.getBooleanFlagValue(87);
            enableDesktopAppLaunchAlttabTransitions = reader.getBooleanFlagValue(88);
            enableDesktopAppLaunchAlttabTransitionsBugfix = reader.getBooleanFlagValue(89);
            enableDesktopAppLaunchBugfix = reader.getBooleanFlagValue(90);
            enableDesktopAppLaunchTransitions = reader.getBooleanFlagValue(91);
            enableDesktopAppLaunchTransitionsBugfix = reader.getBooleanFlagValue(92);
            enableDesktopCloseShortcutBugfix = reader.getBooleanFlagValue(93);
            enableDesktopCloseTaskAnimationInDtcBugfix = reader.getBooleanFlagValue(94);
            enableDesktopFirstBasedDefaultToDesktopBugfix = reader.getBooleanFlagValue(95);
            enableDesktopFirstBasedDragToMaximize = reader.getBooleanFlagValue(96);
            enableDesktopFirstFullscreenRefocusBugfix = reader.getBooleanFlagValue(97);
            enableDesktopFirstListener = reader.getBooleanFlagValue(98);
            enableDesktopFirstPolicyInLpm = reader.getBooleanFlagValue(99);
            enableDesktopFirstTopFullscreenBugfix = reader.getBooleanFlagValue(100);
            enableDesktopImeBugfix = reader.getBooleanFlagValue(101);
            enableDesktopImmersiveDragBugfix = reader.getBooleanFlagValue(102);
            enableDesktopIndicatorInSeparateThreadBugfix = reader.getBooleanFlagValue(103);
            enableDesktopInvisibleTaskRemovalCleanupBugfix = reader.getBooleanFlagValue(104);
            enableDesktopModeThroughDevOption = reader.getBooleanFlagValue(105);
            enableDesktopOpeningDeeplinkMinimizeAnimationBugfix = reader.getBooleanFlagValue(106);
            enableDesktopRecentsTransitionsCornersBugfix = reader.getBooleanFlagValue(107);
            enableDesktopSplitscreenTransitionBugfix = reader.getBooleanFlagValue(108);
            enableDesktopSystemDialogsTransitions = reader.getBooleanFlagValue(109);
            enableDesktopTabTearingLaunchAnimation = reader.getBooleanFlagValue(110);
            enableDesktopTabTearingMinimizeAnimationBugfix = reader.getBooleanFlagValue(111);
            enableDesktopTaskLimitSeparateTransition = reader.getBooleanFlagValue(112);
            enableDesktopTaskbarOnFreeformDisplays = reader.getBooleanFlagValue(113);
            enableDesktopTrampolineCloseAnimationBugfix = reader.getBooleanFlagValue(114);
            enableDesktopWallpaperActivityForSystemUser = reader.getBooleanFlagValue(115);
            enableDesktopWindowingAppHandleEducation = reader.getBooleanFlagValue(116);
            enableDesktopWindowingAppToWeb = reader.getBooleanFlagValue(117);
            enableDesktopWindowingAppToWebEducation = reader.getBooleanFlagValue(118);
            enableDesktopWindowingAppToWebEducationIntegration = reader.getBooleanFlagValue(119);
            enableDesktopWindowingBackNavigation = reader.getBooleanFlagValue(120);
            enableDesktopWindowingEnterTransitionBugfix = reader.getBooleanFlagValue(121);
            enableDesktopWindowingEnterTransitions = reader.getBooleanFlagValue(122);
            enableDesktopWindowingEnterpriseBugfix = reader.getBooleanFlagValue(123);
            enableDesktopWindowingExitByMinimizeTransitionBugfix = reader.getBooleanFlagValue(124);
            enableDesktopWindowingExitTransitions = reader.getBooleanFlagValue(125);
            enableDesktopWindowingExitTransitionsBugfix = reader.getBooleanFlagValue(126);
            enableDesktopWindowingHsum = reader.getBooleanFlagValue(127);
            enableDesktopWindowingImmersiveHandleHiding = reader.getBooleanFlagValue(128);
            enableDesktopWindowingModalsPolicy = reader.getBooleanFlagValue(129);
            enableDesktopWindowingMode = reader.getBooleanFlagValue(130);
            enableDesktopWindowingMultiInstanceFeatures = reader.getBooleanFlagValue(131);
            enableDesktopWindowingPersistence = reader.getBooleanFlagValue(132);
            enableDesktopWindowingPip = reader.getBooleanFlagValue(133);
            enableDesktopWindowingPipInOverviewBugfix = reader.getBooleanFlagValue(134);
            enableDesktopWindowingQuickSwitch = reader.getBooleanFlagValue(135);
            enableDesktopWindowingScvhCacheBugFix = reader.getBooleanFlagValue(136);
            enableDesktopWindowingSizeConstraints = reader.getBooleanFlagValue(137);
            enableDesktopWindowingTaskLimit = reader.getBooleanFlagValue(138);
            enableDesktopWindowingTaskbarRunningApps = reader.getBooleanFlagValue(139);
            enableDesktopWindowingTransitions = reader.getBooleanFlagValue(140);
            enableDesktopWindowingWallpaperActivity = reader.getBooleanFlagValue(141);
            enableDialogDisplayFixes = reader.getBooleanFlagValue(144);
            enableDisplayCompatMode = reader.getBooleanFlagValue(145);
            enableDisplayDisconnectInteraction = reader.getBooleanFlagValue(146);
            enableDisplayFocusInShellTransitions = reader.getBooleanFlagValue(147);
            enableDisplayReconnectInteraction = reader.getBooleanFlagValue(148);
            enableDisplayWindowingModeSwitching = reader.getBooleanFlagValue(149);
            enableDragEndStableBoundsReset = reader.getBooleanFlagValue(150);
            enableDragResizeSetUpInBgThread = reader.getBooleanFlagValue(151);
            enableDragToDesktopIncomingTransitionsBugfix = reader.getBooleanFlagValue(152);
            enableDragToMaximize = reader.getBooleanFlagValue(153);
            enableDraggingPipAcrossDisplays = reader.getBooleanFlagValue(154);
            enableDrawingAppHandle = reader.getBooleanFlagValue(155);
            enableDreamActivityWindowingExclusion = reader.getBooleanFlagValue(156);
            enableDynamicRadiusComputationBugfix = reader.getBooleanFlagValue(157);
            enableEmptyDeskOnMinimize = reader.getBooleanFlagValue(158);
            enableExternalDisplayPersistenceBugfix = reader.getBooleanFlagValue(160);
            enableFreeformBoxShadows = reader.getBooleanFlagValue(161);
            enableFreeformDisplayLaunchParams = reader.getBooleanFlagValue(162);
            enableFullScreenWindowOnRemovingSplitScreenStageBugfix = reader.getBooleanFlagValue(163);
            enableFullscreenWindowControls = reader.getBooleanFlagValue(164);
            enableFullyImmersiveInDesktop = reader.getBooleanFlagValue(165);
            enableHandleInputFix = reader.getBooleanFlagValue(166);
            enableHoldToDragAppHandle = reader.getBooleanFlagValue(168);
            enableIndependentBackInProjected = reader.getBooleanFlagValue(169);
            enableInorderTransitionCallbacksForDesktop = reader.getBooleanFlagValue(170);
            enableInputLayerTransitionFix = reader.getBooleanFlagValue(171);
            enableInteractionDependentTabTearingBounds = reader.getBooleanFlagValue(172);
            enableKeyGestureHandlerForSysui = reader.getBooleanFlagValue(174);
            enableLauncherHandleGoHomeKeyboardShortcut = reader.getBooleanFlagValue(175);
            enableMinimizeButton = reader.getBooleanFlagValue(176);
            enableMirrorDisplayNoActivity = reader.getBooleanFlagValue(177);
            enableModalsFullscreenWithPermission = reader.getBooleanFlagValue(178);
            enableModalsFullscreenWithPlatformSignature = reader.getBooleanFlagValue(179);
            enableMoveToNextDisplayShortcut = reader.getBooleanFlagValue(180);
            enableMultiDisplayHomeFocusBugFix = reader.getBooleanFlagValue(181);
            enableMultiDisplaySplit = reader.getBooleanFlagValue(182);
            enableMultidisplayTrackpadBackGesture = reader.getBooleanFlagValue(183);
            enableMultipleDesktopsBackend = reader.getBooleanFlagValue(184);
            enableMultipleDesktopsDefaultActivationInDesktopFirstDisplays = reader.getBooleanFlagValue(185);
            enableMultipleDesktopsFrontend = reader.getBooleanFlagValue(186);
            enableNoWindowDecorationForDesks = reader.getBooleanFlagValue(187);
            enableNonDefaultDisplaySplit = reader.getBooleanFlagValue(188);
            enableNonDefaultDisplaySplitBugfix = reader.getBooleanFlagValue(189);
            enableOpaqueBackgroundForTransparentWindows = reader.getBooleanFlagValue(190);
            enableOverflowButtonForTaskbarPinnedItems = reader.getBooleanFlagValue(191);
            enablePerDisplayDesktopWallpaperActivity = reader.getBooleanFlagValue(192);
            enablePerDisplayPackageContextCacheInStatusbarNotif = reader.getBooleanFlagValue(193);
            enablePerDisplayWindowDecorViewHostPool = reader.getBooleanFlagValue(194);
            enablePersistingDisplaySizeForConnectedDisplays = reader.getBooleanFlagValue(195);
            enablePinningAppWithContextMenu = reader.getBooleanFlagValue(196);
            enablePipParamsUpdateNotificationBugfix = reader.getBooleanFlagValue(197);
            enablePresentationDisallowedOnUnfocusedHostTask = reader.getBooleanFlagValue(198);
            enablePresentationForConnectedDisplays = reader.getBooleanFlagValue(199);
            enableProjectedDisplayDesktopMode = reader.getBooleanFlagValue(200);
            enableQuickswitchDesktopSplitBugfix = reader.getBooleanFlagValue(201);
            enableRejectHomeTransition = reader.getBooleanFlagValue(202);
            enableRemoveStatusBarInputLayer = reader.getBooleanFlagValue(203);
            enableRequestFullscreenBugfix = reader.getBooleanFlagValue(204);
            enableRequestFullscreenRefactor = reader.getBooleanFlagValue(205);
            enableRequestFullscreenRestoreFreeformBugfix = reader.getBooleanFlagValue(206);
            enableResizingMetrics = reader.getBooleanFlagValue(207);
            enableRestartMenuForConnectedDisplays = reader.getBooleanFlagValue(208);
            enableRestoreToPreviousSizeFromDesktopImmersive = reader.getBooleanFlagValue(209);
            enableSeeThroughTaskFragments = reader.getBooleanFlagValue(210);
            enableShellInitialBoundsRegressionBugFix = reader.getBooleanFlagValue(211);
            enableShrinkWindowBoundsAfterDrag = reader.getBooleanFlagValue(212);
            enableSizeCompatModeImprovementsForConnectedDisplays = reader.getBooleanFlagValue(213);
            enableStartLaunchTransitionFromTaskbarBugfix = reader.getBooleanFlagValue(214);
            enableSysDecorsCallbacksViaWm = reader.getBooleanFlagValue(215);
            enableTallAppHeaders = reader.getBooleanFlagValue(216);
            enableTaskResizingKeyboardShortcuts = reader.getBooleanFlagValue(217);
            enableTaskStackObserverInShell = reader.getBooleanFlagValue(218);
            enableTaskbarConnectedDisplays = reader.getBooleanFlagValue(219);
            enableTaskbarOverflow = reader.getBooleanFlagValue(220);
            enableTaskbarRecentTasksThrottleBugfix = reader.getBooleanFlagValue(221);
            enableTaskbarRecentsLayoutTransition = reader.getBooleanFlagValue(222);
            enableThemedAppHeaders = reader.getBooleanFlagValue(223);
            enableTileResizing = reader.getBooleanFlagValue(224);
            enableUpdatedDisplayConnectionDialog = reader.getBooleanFlagValue(227);
            enableUpscalingSizeCompatOnExitingDesktopBugfix = reader.getBooleanFlagValue(228);
            enableVisualIndicatorInTransitionBugfix = reader.getBooleanFlagValue(229);
            enableWindowDecorationRefactor = reader.getBooleanFlagValue(231);
            enableWindowDropSmoothTransition = reader.getBooleanFlagValue(232);
            enableWindowRepositioningApi = reader.getBooleanFlagValue(233);
            enableWindowingDynamicInitialBounds = reader.getBooleanFlagValue(234);
            enableWindowingEdgeDragResize = reader.getBooleanFlagValue(235);
            enableWindowingScaledResizing = reader.getBooleanFlagValue(236);
            enableWindowingTaskStackOrderBugfix = reader.getBooleanFlagValue(237);
            enableWindowingTransitionHandlersObservers = reader.getBooleanFlagValue(238);
            enterDesktopByDefaultOnFreeformDisplays = reader.getBooleanFlagValue(241);
            excludeCaptionFromAppBounds = reader.getBooleanFlagValue(242);
            excludeDeskRootsFromDesktopTasks = reader.getBooleanFlagValue(243);
            fixLeakingVisualIndicator = reader.getBooleanFlagValue(250);
            forceCloseTopTransparentFullscreenTask = reader.getBooleanFlagValue(252);
            formFactorBasedDesktopFirstSwitch = reader.getBooleanFlagValue(253);
            handleIncompatibleTasksInDesktopLaunchParams = reader.getBooleanFlagValue(255);
            ignoreCurrentParamsInDesktopLaunchParams = reader.getBooleanFlagValue(258);
            ignoreOverrideTaskBoundsIfIncompatibleWithDisplay = reader.getBooleanFlagValue(259);
            includeTopTransparentFullscreenTaskInDesktopHeuristic = reader.getBooleanFlagValue(261);
            inheritTaskBoundsForTrampolineTaskLaunches = reader.getBooleanFlagValue(262);
            keyboardShortcutsToSwitchDesks = reader.getBooleanFlagValue(264);
            limitSystemFullscreenOverrideToDefaultDisplay = reader.getBooleanFlagValue(267);
            moveToNextDisplayShortcutWithProjectedMode = reader.getBooleanFlagValue(270);
            nestedTasksWithIndependentBoundsBugfix = reader.getBooleanFlagValue(273);
            parallelCdTransitionsDuringRecents = reader.getBooleanFlagValue(276);
            preserveRecentsTaskConfigurationOnRelaunch = reader.getBooleanFlagValue(287);
            reenableAppHandleAnimations = reader.getBooleanFlagValue(291);
            reenableAppHandleColorAnimations = reader.getBooleanFlagValue(292);
            removeDeskOnLastTaskRemoval = reader.getBooleanFlagValue(295);
            repositoryBasedPersistence = reader.getBooleanFlagValue(298);
            repositoryBasedPersistenceBgThread = reader.getBooleanFlagValue(299);
            respectFullscreenActivityOptionInDesktopLaunchParams = reader.getBooleanFlagValue(300);
            respectOrientationChangeForUnresizeable = reader.getBooleanFlagValue(301);
            restrictFreeformHiddenSystemBarsToFillingTasks = reader.getBooleanFlagValue(304);
            showBiometricPromptSecondaryDisplayMessage = reader.getBooleanFlagValue(314);
            showDesktopExperienceDevOption = reader.getBooleanFlagValue(315);
            showDesktopWindowingDevOption = reader.getBooleanFlagValue(316);
            showHomeBehindDesktop = reader.getBooleanFlagValue(317);
            skipCompatUiEducationInDesktopMode = reader.getBooleanFlagValue(318);
            skipDeactivationOfDeskWithNothingInFront = reader.getBooleanFlagValue(319);
            skipDecorViewRelayoutWhenClosingBugfix = reader.getBooleanFlagValue(320);
            taskbarRunningTasksInSplitscreenSelect = reader.getBooleanFlagValue(325);
            toggleFullscreenStateViaFullscreenKey = reader.getBooleanFlagValue(326);
            balAdditionalStartModes = reader.getBooleanFlagValue(24);
            balCheckBroadcastWhenDispatched = reader.getBooleanFlagValue(25);
            balCoverIntentSender = reader.getBooleanFlagValue(26);
            balDontBringExistingBackgroundTaskStackToFg = reader.getBooleanFlagValue(27);
            balReduceGracePeriod = reader.getBooleanFlagValue(28);
            balReportAbortedActivityStarts = reader.getBooleanFlagValue(29);
            balRespectAppSwitchStateWhenCheckBoundByForegroundUid = reader.getBooleanFlagValue(30);
            balSendIntentWithOptions = reader.getBooleanFlagValue(31);
            avoidRebindingIntentionallyDisconnectedWallpaper = reader.getBooleanFlagValue(22);
            multiCrop = reader.getBooleanFlagValue(271);
            alwaysUpdateWallpaperPermission = reader.getBooleanFlagValue(8);
            currentAnimatorScaleUsesSharedMemory = reader.getBooleanFlagValue(41);
            enableInteractivePictureInPicture = reader.getBooleanFlagValue(173);
            deprecateSurfaceAnimationFrameCallback = reader.getBooleanFlagValue(45);
            deprecateWindowAnimatorFrameCallback = reader.getBooleanFlagValue(46);
            systemContentPriority = reader.getBooleanFlagValue(322);
            useInputReportedFocusForAccessibility = reader.getBooleanFlagValue(337);
            alwaysSeqIdLayout = reader.getBooleanFlagValue(6);
            alwaysSeqIdLayoutWear = reader.getBooleanFlagValue(7);
            aodTransition = reader.getBooleanFlagValue(9);
            appHandleNoRelayoutOnExclusionChange = reader.getBooleanFlagValue(19);
            applyLifecycleOnPipChange = reader.getBooleanFlagValue(21);
            closeToSquareConfigIncludesStatusBar = reader.getBooleanFlagValue(39);
            enableDeviceStateAutoRotateSettingLogging = reader.getBooleanFlagValue(142);
            enableDeviceStateAutoRotateSettingRefactor = reader.getBooleanFlagValue(143);
            enableHandlersDebuggingMode = reader.getBooleanFlagValue(167);
            ensureWallpaperDrawnOnDisplaySwitch = reader.getBooleanFlagValue(240);
            excludingLayerFromTaskSnapshot = reader.getBooleanFlagValue(244);
            fallbackTransitionPlayer = reader.getBooleanFlagValue(246);
            keyguardRemoveDefaultDisplayUsage = reader.getBooleanFlagValue(265);
            migrateBasicLegacyReady = reader.getBooleanFlagValue(268);
            multipleSystemNavigationObserverCallbacks = reader.getBooleanFlagValue(272);
            noAlphaRotationEnterAnimation = reader.getBooleanFlagValue(274);
            optOutOverrideOrientationToUser = reader.getBooleanFlagValue(275);
            polishCloseWallpaperIncludesOpenChange = reader.getBooleanFlagValue(277);
            portWindowSizeAnimation = reader.getBooleanFlagValue(278);
            predictiveBackCallbackCancellationFix = reader.getBooleanFlagValue(279);
            predictiveBackDelayWmTransition = reader.getBooleanFlagValue(280);
            predictiveBackInterceptTransition = reader.getBooleanFlagValue(281);
            predictiveBackStopKeycodeBackForwarding = reader.getBooleanFlagValue(283);
            removeGetDimmer = reader.getBooleanFlagValue(296);
            respectRequestedTaskSnapshotResolution = reader.getBooleanFlagValue(302);
            scvhSetFocusable = reader.getBooleanFlagValue(309);
            showAppHandleLargeScreens = reader.getBooleanFlagValue(313);
            systemUiPostAnimationEnd = reader.getBooleanFlagValue(323);
            transferStartingWindowToNextWhenInvisible = reader.getBooleanFlagValue(328);
            transitReadyTracking = reader.getBooleanFlagValue(329);
            unifyShellBinders = reader.getBooleanFlagValue(331);
            universalResizableByDefault = reader.getBooleanFlagValue(332);
            updateTaskCropInSync = reader.getBooleanFlagValue(336);
            waitForPresentFenceOnDisplaySwitch = reader.getBooleanFlagValue(339);
            activityEmbeddingAbortCrossUidLaunchInFinishingTaskFragment = reader.getBooleanFlagValue(0);
            activityEmbeddingDelayTaskFragmentFinishForActivityLaunch = reader.getBooleanFlagValue(1);
            activityEmbeddingInteractiveDividerFlag = reader.getBooleanFlagValue(2);
            activityEmbeddingMetrics = reader.getBooleanFlagValue(3);
            enableExperimentalBubblesController = reader.getBooleanFlagValue(159);
            fixBalReparentExistingTask = reader.getBooleanFlagValue(247);
            fixBubbleTrampolineAnimation = reader.getBooleanFlagValue(248);
            fixRapidTopResumedSwitch = reader.getBooleanFlagValue(251);
            refactorMatchParentBounds = reader.getBooleanFlagValue(293);
            touchPassThroughOptIn = reader.getBooleanFlagValue(327);
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

    public boolean activityEmbeddingAbortCrossUidLaunchInFinishingTaskFragment() {
        if (!isCached) {
            init();
        }
        return activityEmbeddingAbortCrossUidLaunchInFinishingTaskFragment;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean activityEmbeddingDelayTaskFragmentFinishForActivityLaunch() {
        if (!isCached) {
            init();
        }
        return activityEmbeddingDelayTaskFragmentFinishForActivityLaunch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean activityEmbeddingInteractiveDividerFlag() {
        if (!isCached) {
            init();
        }
        return activityEmbeddingInteractiveDividerFlag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean activityEmbeddingMetrics() {
        if (!isCached) {
            init();
        }
        return activityEmbeddingMetrics;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowDisableActivityRecordInputSink() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean alwaysDrawMagnificationFullscreenBorder() {
        if (!isCached) {
            init();
        }
        return alwaysDrawMagnificationFullscreenBorder;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean alwaysSeqIdLayout() {
        if (!isCached) {
            init();
        }
        return alwaysSeqIdLayout;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean alwaysSeqIdLayoutWear() {
        if (!isCached) {
            init();
        }
        return alwaysSeqIdLayoutWear;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean alwaysUpdateWallpaperPermission() {
        if (!isCached) {
            init();
        }
        return alwaysUpdateWallpaperPermission;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean aodTransition() {
        if (!isCached) {
            init();
        }
        return aodTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatPropertiesApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoring() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringFixMultiwindowTaskHierarchy() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringFixMultiwindowTaskHierarchy;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringForceChangeForLetterboxTransitions() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringForceChangeForLetterboxTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringRoundedCorners() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringRoundedCorners;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringRoundedCornersAnimation() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringRoundedCornersAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringSetAppboundsToNullWhenEmpty() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringSetAppboundsToNullWhenEmpty;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringSkipStartingWindowLetterbox() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringSkipStartingWindowLetterbox;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatRefactoringUseActivityLeashForLetterboxing() {
        if (!isCached) {
            init();
        }
        return appCompatRefactoringUseActivityLeashForLetterboxing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appCompatUiFramework() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appHandleNoRelayoutOnExclusionChange() {
        if (!isCached) {
            init();
        }
        return appHandleNoRelayoutOnExclusionChange;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean applyDeskActivationOnUserSwitch() {
        if (!isCached) {
            init();
        }
        return applyDeskActivationOnUserSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean applyLifecycleOnPipChange() {
        if (!isCached) {
            init();
        }
        return applyLifecycleOnPipChange;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean avoidRebindingIntentionallyDisconnectedWallpaper() {
        if (!isCached) {
            init();
        }
        return avoidRebindingIntentionallyDisconnectedWallpaper;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean backupAndRestoreForUserAspectRatioSettings() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balAdditionalStartModes() {
        if (!isCached) {
            init();
        }
        return balAdditionalStartModes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balCheckBroadcastWhenDispatched() {
        if (!isCached) {
            init();
        }
        return balCheckBroadcastWhenDispatched;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balCoverIntentSender() {
        if (!isCached) {
            init();
        }
        return balCoverIntentSender;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balDontBringExistingBackgroundTaskStackToFg() {
        if (!isCached) {
            init();
        }
        return balDontBringExistingBackgroundTaskStackToFg;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balReduceGracePeriod() {
        if (!isCached) {
            init();
        }
        return balReduceGracePeriod;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balReportAbortedActivityStarts() {
        if (!isCached) {
            init();
        }
        return balReportAbortedActivityStarts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balRespectAppSwitchStateWhenCheckBoundByForegroundUid() {
        if (!isCached) {
            init();
        }
        return balRespectAppSwitchStateWhenCheckBoundByForegroundUid;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balSendIntentWithOptions() {
        if (!isCached) {
            init();
        }
        return balSendIntentWithOptions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean balStrictModeRo() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cameraCompatFullscreenPickSameTaskActivity() {
        if (!isCached) {
            init();
        }
        return cameraCompatFullscreenPickSameTaskActivity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cameraCompatLandscapeCameraSupport() {
        if (!isCached) {
            init();
        }
        return cameraCompatLandscapeCameraSupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cameraCompatUnifyCameraPolicies() {
        if (!isCached) {
            init();
        }
        return cameraCompatUnifyCameraPolicies;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clearReusableScvhOnRelease() {
        if (!isCached) {
            init();
        }
        return clearReusableScvhOnRelease;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean closeFullscreenAndSplitscreenKeyboardShortcut() {
        if (!isCached) {
            init();
        }
        return closeFullscreenAndSplitscreenKeyboardShortcut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean closeTaskKeyboardShortcut() {
        if (!isCached) {
            init();
        }
        return closeTaskKeyboardShortcut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean closeToSquareConfigIncludesStatusBar() {
        if (!isCached) {
            init();
        }
        return closeToSquareConfigIncludesStatusBar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean coverDisplayOptIn() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean currentAnimatorScaleUsesSharedMemory() {
        if (!isCached) {
            init();
        }
        return currentAnimatorScaleUsesSharedMemory;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean defaultDeskWithoutWarmupMigration() {
        if (!isCached) {
            init();
        }
        return defaultDeskWithoutWarmupMigration;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deferResumeFocusInNonFocusedWindow() {
        if (!isCached) {
            init();
        }
        return deferResumeFocusInNonFocusedWindow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean density390Api() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deprecateSurfaceAnimationFrameCallback() {
        if (!isCached) {
            init();
        }
        return deprecateSurfaceAnimationFrameCallback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deprecateWindowAnimatorFrameCallback() {
        if (!isCached) {
            init();
        }
        return deprecateWindowAnimatorFrameCallback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dimmingWallpaperForMaximizedAndTiled() {
        if (!isCached) {
            init();
        }
        return dimmingWallpaperForMaximizedAndTiled;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableDesktopLaunchParamsOutsideDesktopBugFix() {
        if (!isCached) {
            init();
        }
        return disableDesktopLaunchParamsOutsideDesktopBugFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableNonResizableAppSnapResizing() {
        if (!isCached) {
            init();
        }
        return disableNonResizableAppSnapResizing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableRestoreNonFullscreenBoundsOnConfigurationChange() {
        if (!isCached) {
            init();
        }
        return disableRestoreNonFullscreenBoundsOnConfigurationChange;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean doNotForceWallpaperForFreeformTask() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAccessibleCustomHeaders() {
        if (!isCached) {
            init();
        }
        return enableAccessibleCustomHeaders;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableActivityEmbeddingSupportForConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableActivityEmbeddingSupportForConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAppHandlePositionReporting() {
        if (!isCached) {
            init();
        }
        return enableAppHandlePositionReporting;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAppHeaderWithTaskDensity() {
        if (!isCached) {
            init();
        }
        return enableAppHeaderWithTaskDensity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAppToWebEducationAnimation() {
        if (!isCached) {
            init();
        }
        return enableAppToWebEducationAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAutoRestartOnDisplayMove() {
        if (!isCached) {
            init();
        }
        return enableAutoRestartOnDisplayMove;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBackupAndRestoreDisplayWindowSettings() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBlockNonDesktopDisplayWindowDragBugfix() {
        if (!isCached) {
            init();
        }
        return enableBlockNonDesktopDisplayWindowDragBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBorderSettings() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBoundsRestoringOnTilingExit() {
        if (!isCached) {
            init();
        }
        return enableBoundsRestoringOnTilingExit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBoxShadowSettings() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBugFixesForSecondaryDisplay() {
        if (!isCached) {
            init();
        }
        return enableBugFixesForSecondaryDisplay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatCompatibilityInfoRotateAndCropBugfix() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatCompatibilityInfoRotateAndCropBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatExternalDisplayRotationBugfix() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatExternalDisplayRotationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatForDesktopWindowing() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatForDesktopWindowing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatForDesktopWindowingOptOut() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatForDesktopWindowingOptOut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatForDesktopWindowingOptOutApi() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatForDesktopWindowingOptOutApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatSandboxDisplayRotationOnExternalDisplaysBugfix() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatSandboxDisplayRotationOnExternalDisplaysBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCameraCompatTrackTaskAndAppBugfix() {
        if (!isCached) {
            init();
        }
        return enableCameraCompatTrackTaskAndAppBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCaptionCompatInsetConversion() {
        if (!isCached) {
            init();
        }
        return enableCaptionCompatInsetConversion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCaptionCompatInsetForceConsumption() {
        if (!isCached) {
            init();
        }
        return enableCaptionCompatInsetForceConsumption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCaptionCompatInsetForceConsumptionAlways() {
        if (!isCached) {
            init();
        }
        return enableCaptionCompatInsetForceConsumptionAlways;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCascadingWindows() {
        if (!isCached) {
            init();
        }
        return enableCascadingWindows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCloseLidInteraction() {
        if (!isCached) {
            init();
        }
        return enableCloseLidInteraction;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCompatUiDesktopModeSynchronizationBugfix() {
        if (!isCached) {
            init();
        }
        return enableCompatUiDesktopModeSynchronizationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCompatUiVisibilityStatus() {
        if (!isCached) {
            init();
        }
        return enableCompatUiVisibilityStatus;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCompatuiSysuiLauncherFix() {
        if (!isCached) {
            init();
        }
        return enableCompatuiSysuiLauncherFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableConnectedDisplaysDnd() {
        if (!isCached) {
            init();
        }
        return enableConnectedDisplaysDnd;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableConnectedDisplaysPip() {
        if (!isCached) {
            init();
        }
        return enableConnectedDisplaysPip;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableConnectedDisplaysWallpaperPresentations() {
        if (!isCached) {
            init();
        }
        return enableConnectedDisplaysWallpaperPresentations;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableConnectedDisplaysWindowDrag() {
        if (!isCached) {
            init();
        }
        return enableConnectedDisplaysWindowDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCrashLoggingForDesktop() {
        if (!isCached) {
            init();
        }
        return enableCrashLoggingForDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCrossDisplaysAppLaunchTransition() {
        if (!isCached) {
            init();
        }
        return enableCrossDisplaysAppLaunchTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCrossDisplaysPipTaskLaunch() {
        if (!isCached) {
            init();
        }
        return enableCrossDisplaysPipTaskLaunch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppHandleAnimation() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppHandleAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppHeaderStateChangeAnnouncements() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppHeaderStateChangeAnnouncements;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppLaunchAlttabTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppLaunchAlttabTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppLaunchAlttabTransitionsBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppLaunchAlttabTransitionsBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppLaunchBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppLaunchBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppLaunchTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppLaunchTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopAppLaunchTransitionsBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopAppLaunchTransitionsBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopCloseShortcutBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopCloseShortcutBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopCloseTaskAnimationInDtcBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopCloseTaskAnimationInDtcBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstBasedDefaultToDesktopBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstBasedDefaultToDesktopBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstBasedDragToMaximize() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstBasedDragToMaximize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstFullscreenRefocusBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstFullscreenRefocusBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstListener() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstListener;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstPolicyInLpm() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstPolicyInLpm;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopFirstTopFullscreenBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopFirstTopFullscreenBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopImeBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopImeBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopImmersiveDragBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopImmersiveDragBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopIndicatorInSeparateThreadBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopIndicatorInSeparateThreadBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopInvisibleTaskRemovalCleanupBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopInvisibleTaskRemovalCleanupBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopModeThroughDevOption() {
        if (!isCached) {
            init();
        }
        return enableDesktopModeThroughDevOption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopOpeningDeeplinkMinimizeAnimationBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopOpeningDeeplinkMinimizeAnimationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopRecentsTransitionsCornersBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopRecentsTransitionsCornersBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopSplitscreenTransitionBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopSplitscreenTransitionBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopSystemDialogsTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopSystemDialogsTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopTabTearingLaunchAnimation() {
        if (!isCached) {
            init();
        }
        return enableDesktopTabTearingLaunchAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopTabTearingMinimizeAnimationBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopTabTearingMinimizeAnimationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopTaskLimitSeparateTransition() {
        if (!isCached) {
            init();
        }
        return enableDesktopTaskLimitSeparateTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopTaskbarOnFreeformDisplays() {
        if (!isCached) {
            init();
        }
        return enableDesktopTaskbarOnFreeformDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopTrampolineCloseAnimationBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopTrampolineCloseAnimationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWallpaperActivityForSystemUser() {
        if (!isCached) {
            init();
        }
        return enableDesktopWallpaperActivityForSystemUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingAppHandleEducation() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingAppHandleEducation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingAppToWeb() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingAppToWeb;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingAppToWebEducation() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingAppToWebEducation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingAppToWebEducationIntegration() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingAppToWebEducationIntegration;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingBackNavigation() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingBackNavigation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingEnterTransitionBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingEnterTransitionBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingEnterTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingEnterTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingEnterpriseBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingEnterpriseBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingExitByMinimizeTransitionBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingExitByMinimizeTransitionBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingExitTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingExitTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingExitTransitionsBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingExitTransitionsBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingHsum() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingHsum;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingImmersiveHandleHiding() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingImmersiveHandleHiding;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingModalsPolicy() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingModalsPolicy;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingMode() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingMultiInstanceFeatures() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingMultiInstanceFeatures;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingPersistence() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingPersistence;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingPip() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingPip;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingPipInOverviewBugfix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingPipInOverviewBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingQuickSwitch() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingQuickSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingScvhCacheBugFix() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingScvhCacheBugFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingSizeConstraints() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingSizeConstraints;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingTaskLimit() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingTaskLimit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingTaskbarRunningApps() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingTaskbarRunningApps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingTransitions() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopWindowingWallpaperActivity() {
        if (!isCached) {
            init();
        }
        return enableDesktopWindowingWallpaperActivity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDeviceStateAutoRotateSettingLogging() {
        if (!isCached) {
            init();
        }
        return enableDeviceStateAutoRotateSettingLogging;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDeviceStateAutoRotateSettingRefactor() {
        if (!isCached) {
            init();
        }
        return enableDeviceStateAutoRotateSettingRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDialogDisplayFixes() {
        if (!isCached) {
            init();
        }
        return enableDialogDisplayFixes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDisplayCompatMode() {
        if (!isCached) {
            init();
        }
        return enableDisplayCompatMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDisplayDisconnectInteraction() {
        if (!isCached) {
            init();
        }
        return enableDisplayDisconnectInteraction;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDisplayFocusInShellTransitions() {
        if (!isCached) {
            init();
        }
        return enableDisplayFocusInShellTransitions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDisplayReconnectInteraction() {
        if (!isCached) {
            init();
        }
        return enableDisplayReconnectInteraction;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDisplayWindowingModeSwitching() {
        if (!isCached) {
            init();
        }
        return enableDisplayWindowingModeSwitching;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDragEndStableBoundsReset() {
        if (!isCached) {
            init();
        }
        return enableDragEndStableBoundsReset;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDragResizeSetUpInBgThread() {
        if (!isCached) {
            init();
        }
        return enableDragResizeSetUpInBgThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDragToDesktopIncomingTransitionsBugfix() {
        if (!isCached) {
            init();
        }
        return enableDragToDesktopIncomingTransitionsBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDragToMaximize() {
        if (!isCached) {
            init();
        }
        return enableDragToMaximize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDraggingPipAcrossDisplays() {
        if (!isCached) {
            init();
        }
        return enableDraggingPipAcrossDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDrawingAppHandle() {
        if (!isCached) {
            init();
        }
        return enableDrawingAppHandle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDreamActivityWindowingExclusion() {
        if (!isCached) {
            init();
        }
        return enableDreamActivityWindowingExclusion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDynamicRadiusComputationBugfix() {
        if (!isCached) {
            init();
        }
        return enableDynamicRadiusComputationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableEmptyDeskOnMinimize() {
        if (!isCached) {
            init();
        }
        return enableEmptyDeskOnMinimize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableExperimentalBubblesController() {
        if (!isCached) {
            init();
        }
        return enableExperimentalBubblesController;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableExternalDisplayPersistenceBugfix() {
        if (!isCached) {
            init();
        }
        return enableExternalDisplayPersistenceBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFreeformBoxShadows() {
        if (!isCached) {
            init();
        }
        return enableFreeformBoxShadows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFreeformDisplayLaunchParams() {
        if (!isCached) {
            init();
        }
        return enableFreeformDisplayLaunchParams;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFullScreenWindowOnRemovingSplitScreenStageBugfix() {
        if (!isCached) {
            init();
        }
        return enableFullScreenWindowOnRemovingSplitScreenStageBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFullscreenWindowControls() {
        if (!isCached) {
            init();
        }
        return enableFullscreenWindowControls;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableFullyImmersiveInDesktop() {
        if (!isCached) {
            init();
        }
        return enableFullyImmersiveInDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHandleInputFix() {
        if (!isCached) {
            init();
        }
        return enableHandleInputFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHandlersDebuggingMode() {
        if (!isCached) {
            init();
        }
        return enableHandlersDebuggingMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHoldToDragAppHandle() {
        if (!isCached) {
            init();
        }
        return enableHoldToDragAppHandle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableIndependentBackInProjected() {
        if (!isCached) {
            init();
        }
        return enableIndependentBackInProjected;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableInorderTransitionCallbacksForDesktop() {
        if (!isCached) {
            init();
        }
        return enableInorderTransitionCallbacksForDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableInputLayerTransitionFix() {
        if (!isCached) {
            init();
        }
        return enableInputLayerTransitionFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableInteractionDependentTabTearingBounds() {
        if (!isCached) {
            init();
        }
        return enableInteractionDependentTabTearingBounds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableInteractivePictureInPicture() {
        if (!isCached) {
            init();
        }
        return enableInteractivePictureInPicture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableKeyGestureHandlerForSysui() {
        if (!isCached) {
            init();
        }
        return enableKeyGestureHandlerForSysui;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLauncherHandleGoHomeKeyboardShortcut() {
        if (!isCached) {
            init();
        }
        return enableLauncherHandleGoHomeKeyboardShortcut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMinimizeButton() {
        if (!isCached) {
            init();
        }
        return enableMinimizeButton;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMirrorDisplayNoActivity() {
        if (!isCached) {
            init();
        }
        return enableMirrorDisplayNoActivity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableModalsFullscreenWithPermission() {
        if (!isCached) {
            init();
        }
        return enableModalsFullscreenWithPermission;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableModalsFullscreenWithPlatformSignature() {
        if (!isCached) {
            init();
        }
        return enableModalsFullscreenWithPlatformSignature;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMoveToNextDisplayShortcut() {
        if (!isCached) {
            init();
        }
        return enableMoveToNextDisplayShortcut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultiDisplayHomeFocusBugFix() {
        if (!isCached) {
            init();
        }
        return enableMultiDisplayHomeFocusBugFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultiDisplaySplit() {
        if (!isCached) {
            init();
        }
        return enableMultiDisplaySplit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultidisplayTrackpadBackGesture() {
        if (!isCached) {
            init();
        }
        return enableMultidisplayTrackpadBackGesture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultipleDesktopsBackend() {
        if (!isCached) {
            init();
        }
        return enableMultipleDesktopsBackend;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultipleDesktopsDefaultActivationInDesktopFirstDisplays() {
        if (!isCached) {
            init();
        }
        return enableMultipleDesktopsDefaultActivationInDesktopFirstDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMultipleDesktopsFrontend() {
        if (!isCached) {
            init();
        }
        return enableMultipleDesktopsFrontend;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNoWindowDecorationForDesks() {
        if (!isCached) {
            init();
        }
        return enableNoWindowDecorationForDesks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNonDefaultDisplaySplit() {
        if (!isCached) {
            init();
        }
        return enableNonDefaultDisplaySplit;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableNonDefaultDisplaySplitBugfix() {
        if (!isCached) {
            init();
        }
        return enableNonDefaultDisplaySplitBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOpaqueBackgroundForTransparentWindows() {
        if (!isCached) {
            init();
        }
        return enableOpaqueBackgroundForTransparentWindows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOverflowButtonForTaskbarPinnedItems() {
        if (!isCached) {
            init();
        }
        return enableOverflowButtonForTaskbarPinnedItems;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePerDisplayDesktopWallpaperActivity() {
        if (!isCached) {
            init();
        }
        return enablePerDisplayDesktopWallpaperActivity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePerDisplayPackageContextCacheInStatusbarNotif() {
        if (!isCached) {
            init();
        }
        return enablePerDisplayPackageContextCacheInStatusbarNotif;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePerDisplayWindowDecorViewHostPool() {
        if (!isCached) {
            init();
        }
        return enablePerDisplayWindowDecorViewHostPool;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePersistingDisplaySizeForConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enablePersistingDisplaySizeForConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePinningAppWithContextMenu() {
        if (!isCached) {
            init();
        }
        return enablePinningAppWithContextMenu;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePipParamsUpdateNotificationBugfix() {
        if (!isCached) {
            init();
        }
        return enablePipParamsUpdateNotificationBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePresentationDisallowedOnUnfocusedHostTask() {
        if (!isCached) {
            init();
        }
        return enablePresentationDisallowedOnUnfocusedHostTask;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePresentationForConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enablePresentationForConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableProjectedDisplayDesktopMode() {
        if (!isCached) {
            init();
        }
        return enableProjectedDisplayDesktopMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableQuickswitchDesktopSplitBugfix() {
        if (!isCached) {
            init();
        }
        return enableQuickswitchDesktopSplitBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRejectHomeTransition() {
        if (!isCached) {
            init();
        }
        return enableRejectHomeTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRemoveStatusBarInputLayer() {
        if (!isCached) {
            init();
        }
        return enableRemoveStatusBarInputLayer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRequestFullscreenBugfix() {
        if (!isCached) {
            init();
        }
        return enableRequestFullscreenBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRequestFullscreenRefactor() {
        if (!isCached) {
            init();
        }
        return enableRequestFullscreenRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRequestFullscreenRestoreFreeformBugfix() {
        if (!isCached) {
            init();
        }
        return enableRequestFullscreenRestoreFreeformBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableResizingMetrics() {
        if (!isCached) {
            init();
        }
        return enableResizingMetrics;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRestartMenuForConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableRestartMenuForConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRestoreToPreviousSizeFromDesktopImmersive() {
        if (!isCached) {
            init();
        }
        return enableRestoreToPreviousSizeFromDesktopImmersive;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSeeThroughTaskFragments() {
        if (!isCached) {
            init();
        }
        return enableSeeThroughTaskFragments;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableShellInitialBoundsRegressionBugFix() {
        if (!isCached) {
            init();
        }
        return enableShellInitialBoundsRegressionBugFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableShrinkWindowBoundsAfterDrag() {
        if (!isCached) {
            init();
        }
        return enableShrinkWindowBoundsAfterDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSizeCompatModeImprovementsForConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableSizeCompatModeImprovementsForConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableStartLaunchTransitionFromTaskbarBugfix() {
        if (!isCached) {
            init();
        }
        return enableStartLaunchTransitionFromTaskbarBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSysDecorsCallbacksViaWm() {
        if (!isCached) {
            init();
        }
        return enableSysDecorsCallbacksViaWm;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTallAppHeaders() {
        if (!isCached) {
            init();
        }
        return enableTallAppHeaders;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskResizingKeyboardShortcuts() {
        if (!isCached) {
            init();
        }
        return enableTaskResizingKeyboardShortcuts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskStackObserverInShell() {
        if (!isCached) {
            init();
        }
        return enableTaskStackObserverInShell;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarConnectedDisplays() {
        if (!isCached) {
            init();
        }
        return enableTaskbarConnectedDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarOverflow() {
        if (!isCached) {
            init();
        }
        return enableTaskbarOverflow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarRecentTasksThrottleBugfix() {
        if (!isCached) {
            init();
        }
        return enableTaskbarRecentTasksThrottleBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskbarRecentsLayoutTransition() {
        if (!isCached) {
            init();
        }
        return enableTaskbarRecentsLayoutTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableThemedAppHeaders() {
        if (!isCached) {
            init();
        }
        return enableThemedAppHeaders;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTileResizing() {
        if (!isCached) {
            init();
        }
        return enableTileResizing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTopVisibleRootTaskPerUserTracking() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTransitionOnActivitySetRequestedOrientation() {
        if (!isCached) {
            init();
        }
        return enableTransitionOnActivitySetRequestedOrientation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableUpdatedDisplayConnectionDialog() {
        if (!isCached) {
            init();
        }
        return enableUpdatedDisplayConnectionDialog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableUpscalingSizeCompatOnExitingDesktopBugfix() {
        if (!isCached) {
            init();
        }
        return enableUpscalingSizeCompatOnExitingDesktopBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableVisualIndicatorInTransitionBugfix() {
        if (!isCached) {
            init();
        }
        return enableVisualIndicatorInTransitionBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowContextResourcesUpdateOnConfigChange() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowDecorationRefactor() {
        if (!isCached) {
            init();
        }
        return enableWindowDecorationRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowDropSmoothTransition() {
        if (!isCached) {
            init();
        }
        return enableWindowDropSmoothTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowRepositioningApi() {
        if (!isCached) {
            init();
        }
        return enableWindowRepositioningApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowingDynamicInitialBounds() {
        if (!isCached) {
            init();
        }
        return enableWindowingDynamicInitialBounds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowingEdgeDragResize() {
        if (!isCached) {
            init();
        }
        return enableWindowingEdgeDragResize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowingScaledResizing() {
        if (!isCached) {
            init();
        }
        return enableWindowingScaledResizing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowingTaskStackOrderBugfix() {
        if (!isCached) {
            init();
        }
        return enableWindowingTaskStackOrderBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableWindowingTransitionHandlersObservers() {
        if (!isCached) {
            init();
        }
        return enableWindowingTransitionHandlersObservers;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enforceEdgeToEdge() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ensureKeyguardDoesTransitionStartingBugFix() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ensureWallpaperDrawnOnDisplaySwitch() {
        if (!isCached) {
            init();
        }
        return ensureWallpaperDrawnOnDisplaySwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enterDesktopByDefaultOnFreeformDisplays() {
        if (!isCached) {
            init();
        }
        return enterDesktopByDefaultOnFreeformDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean excludeCaptionFromAppBounds() {
        if (!isCached) {
            init();
        }
        return excludeCaptionFromAppBounds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean excludeDeskRootsFromDesktopTasks() {
        if (!isCached) {
            init();
        }
        return excludeDeskRootsFromDesktopTasks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean excludingLayerFromTaskSnapshot() {
        if (!isCached) {
            init();
        }
        return excludingLayerFromTaskSnapshot;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fallbackToFocusedDisplay() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fallbackTransitionPlayer() {
        if (!isCached) {
            init();
        }
        return fallbackTransitionPlayer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fifoPriorityForMajorUiProcesses() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBalReparentExistingTask() {
        if (!isCached) {
            init();
        }
        return fixBalReparentExistingTask;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixBubbleTrampolineAnimation() {
        if (!isCached) {
            init();
        }
        return fixBubbleTrampolineAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixHideOverlayApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixLeakingVisualIndicator() {
        if (!isCached) {
            init();
        }
        return fixLeakingVisualIndicator;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixRapidTopResumedSwitch() {
        if (!isCached) {
            init();
        }
        return fixRapidTopResumedSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean forceCloseTopTransparentFullscreenTask() {
        if (!isCached) {
            init();
        }
        return forceCloseTopTransparentFullscreenTask;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean formFactorBasedDesktopFirstSwitch() {
        if (!isCached) {
            init();
        }
        return formFactorBasedDesktopFirstSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean grantManageKeyGesturesToRecents() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean handleIncompatibleTasksInDesktopLaunchParams() {
        if (!isCached) {
            init();
        }
        return handleIncompatibleTasksInDesktopLaunchParams;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ignoreAspectRatioRestrictionsForResizeableFreeformActivities() {
        if (!isCached) {
            init();
        }
        return ignoreAspectRatioRestrictionsForResizeableFreeformActivities;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ignoreCornerRadiusAndShadows() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ignoreCurrentParamsInDesktopLaunchParams() {
        if (!isCached) {
            init();
        }
        return ignoreCurrentParamsInDesktopLaunchParams;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ignoreOverrideTaskBoundsIfIncompatibleWithDisplay() {
        if (!isCached) {
            init();
        }
        return ignoreOverrideTaskBoundsIfIncompatibleWithDisplay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean imeBackCallbackLeakPrevention() {
        if (!isCached) {
            init();
        }
        return imeBackCallbackLeakPrevention;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean includeTopTransparentFullscreenTaskInDesktopHeuristic() {
        if (!isCached) {
            init();
        }
        return includeTopTransparentFullscreenTaskInDesktopHeuristic;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean inheritTaskBoundsForTrampolineTaskLaunches() {
        if (!isCached) {
            init();
        }
        return inheritTaskBoundsForTrampolineTaskLaunches;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean jankApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyboardShortcutsToSwitchDesks() {
        if (!isCached) {
            init();
        }
        return keyboardShortcutsToSwitchDesks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyguardRemoveDefaultDisplayUsage() {
        if (!isCached) {
            init();
        }
        return keyguardRemoveDefaultDisplayUsage;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean letterboxBackgroundWallpaper() {
        if (!isCached) {
            init();
        }
        return letterboxBackgroundWallpaper;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean limitSystemFullscreenOverrideToDefaultDisplay() {
        if (!isCached) {
            init();
        }
        return limitSystemFullscreenOverrideToDefaultDisplay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean migrateBasicLegacyReady() {
        if (!isCached) {
            init();
        }
        return migrateBasicLegacyReady;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean moveToExternalDisplayShortcut() {
        if (!isCached) {
            init();
        }
        return moveToExternalDisplayShortcut;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean moveToNextDisplayShortcutWithProjectedMode() {
        if (!isCached) {
            init();
        }
        return moveToNextDisplayShortcutWithProjectedMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean multiCrop() {
        if (!isCached) {
            init();
        }
        return multiCrop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean multipleSystemNavigationObserverCallbacks() {
        if (!isCached) {
            init();
        }
        return multipleSystemNavigationObserverCallbacks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean nestedTasksWithIndependentBoundsBugfix() {
        if (!isCached) {
            init();
        }
        return nestedTasksWithIndependentBoundsBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean noAlphaRotationEnterAnimation() {
        if (!isCached) {
            init();
        }
        return noAlphaRotationEnterAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean optOutOverrideOrientationToUser() {
        if (!isCached) {
            init();
        }
        return optOutOverrideOrientationToUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean parallelCdTransitionsDuringRecents() {
        if (!isCached) {
            init();
        }
        return parallelCdTransitionsDuringRecents;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean polishCloseWallpaperIncludesOpenChange() {
        if (!isCached) {
            init();
        }
        return polishCloseWallpaperIncludesOpenChange;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean portWindowSizeAnimation() {
        if (!isCached) {
            init();
        }
        return portWindowSizeAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackCallbackCancellationFix() {
        if (!isCached) {
            init();
        }
        return predictiveBackCallbackCancellationFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackDelayWmTransition() {
        if (!isCached) {
            init();
        }
        return predictiveBackDelayWmTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackInterceptTransition() {
        if (!isCached) {
            init();
        }
        return predictiveBackInterceptTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackPrioritySystemNavigationObserver() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackStopKeycodeBackForwarding() {
        if (!isCached) {
            init();
        }
        return predictiveBackStopKeycodeBackForwarding;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackSwipeEdgeNoneApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackSystemOverrideCallback() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackTimestampApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean preserveRecentsTaskConfigurationOnRelaunch() {
        if (!isCached) {
            init();
        }
        return preserveRecentsTaskConfigurationOnRelaunch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean rearDisplayDisableForceDesktopSystemDecorations() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reduceChangedExclusionRectsMsgs() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reduceTaskSnapshotMemoryUsage() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reenableAppHandleAnimations() {
        if (!isCached) {
            init();
        }
        return reenableAppHandleAnimations;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reenableAppHandleColorAnimations() {
        if (!isCached) {
            init();
        }
        return reenableAppHandleColorAnimations;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean refactorMatchParentBounds() {
        if (!isCached) {
            init();
        }
        return refactorMatchParentBounds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean relativeInsets() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean releaseAllTransitionSurfaces() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeDeskOnLastTaskRemoval() {
        if (!isCached) {
            init();
        }
        return removeDeskOnLastTaskRemoval;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeGetDimmer() {
        if (!isCached) {
            init();
        }
        return removeGetDimmer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reparentWindowTokenApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean repositoryBasedPersistence() {
        if (!isCached) {
            init();
        }
        return repositoryBasedPersistence;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean repositoryBasedPersistenceBgThread() {
        if (!isCached) {
            init();
        }
        return repositoryBasedPersistenceBgThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean respectFullscreenActivityOptionInDesktopLaunchParams() {
        if (!isCached) {
            init();
        }
        return respectFullscreenActivityOptionInDesktopLaunchParams;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean respectOrientationChangeForUnresizeable() {
        if (!isCached) {
            init();
        }
        return respectOrientationChangeForUnresizeable;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean respectRequestedTaskSnapshotResolution() {
        if (!isCached) {
            init();
        }
        return respectRequestedTaskSnapshotResolution;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restoreUserAspectRatioSettingsUsingService() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restrictFreeformHiddenSystemBarsToFillingTasks() {
        if (!isCached) {
            init();
        }
        return restrictFreeformHiddenSystemBarsToFillingTasks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean returnAllVisibleActivitiesForVis() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean rootTaskForBubble() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean safeRegionLetterboxingV1() {
        if (!isCached) {
            init();
        }
        return safeRegionLetterboxingV1;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenRecordingCallbacks() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scrollingFromLetterbox() {
        if (!isCached) {
            init();
        }
        return scrollingFromLetterbox;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scvhSetFocusable() {
        if (!isCached) {
            init();
        }
        return scvhSetFocusable;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scvhSurfaceControlLifetimeFix() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sdkDesiredPresentTime() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean setScPropertiesInClient() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showAppHandleLargeScreens() {
        if (!isCached) {
            init();
        }
        return showAppHandleLargeScreens;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showBiometricPromptSecondaryDisplayMessage() {
        if (!isCached) {
            init();
        }
        return showBiometricPromptSecondaryDisplayMessage;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showDesktopExperienceDevOption() {
        if (!isCached) {
            init();
        }
        return showDesktopExperienceDevOption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showDesktopWindowingDevOption() {
        if (!isCached) {
            init();
        }
        return showDesktopWindowingDevOption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showHomeBehindDesktop() {
        if (!isCached) {
            init();
        }
        return showHomeBehindDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean skipCompatUiEducationInDesktopMode() {
        if (!isCached) {
            init();
        }
        return skipCompatUiEducationInDesktopMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean skipDeactivationOfDeskWithNothingInFront() {
        if (!isCached) {
            init();
        }
        return skipDeactivationOfDeskWithNothingInFront;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean skipDecorViewRelayoutWhenClosingBugfix() {
        if (!isCached) {
            init();
        }
        return skipDecorViewRelayoutWhenClosingBugfix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean supportGeminiOnMultiDisplay() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean systemContentPriority() {
        if (!isCached) {
            init();
        }
        return systemContentPriority;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean systemUiPostAnimationEnd() {
        if (!isCached) {
            init();
        }
        return systemUiPostAnimationEnd;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean taskFragmentCompanionActivity() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean taskbarRunningTasksInSplitscreenSelect() {
        if (!isCached) {
            init();
        }
        return taskbarRunningTasksInSplitscreenSelect;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean toggleFullscreenStateViaFullscreenKey() {
        if (!isCached) {
            init();
        }
        return toggleFullscreenStateViaFullscreenKey;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean touchPassThroughOptIn() {
        if (!isCached) {
            init();
        }
        return touchPassThroughOptIn;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean transferStartingWindowToNextWhenInvisible() {
        if (!isCached) {
            init();
        }
        return transferStartingWindowToNextWhenInvisible;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean transitReadyTracking() {
        if (!isCached) {
            init();
        }
        return transitReadyTracking;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean transitTrackerPlumbing() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean transitionHandlerCujTags() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean unifyShellBinders() {
        if (!isCached) {
            init();
        }
        return unifyShellBinders;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean universalResizableByDefault() {
        if (!isCached) {
            init();
        }
        return universalResizableByDefault;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean untrustedEmbeddingAnyAppPermission() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean untrustedEmbeddingStateSharing() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateDimsWhenWindowShown() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateTaskCropInSync() {
        if (!isCached) {
            init();
        }
        return updateTaskCropInSync;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useInputReportedFocusForAccessibility() {
        if (!isCached) {
            init();
        }
        return useInputReportedFocusForAccessibility;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean vdmForceAppUniversalResizableApi() {
        if (!isCached) {
            init();
        }
        return vdmForceAppUniversalResizableApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean waitForPresentFenceOnDisplaySwitch() {
        if (!isCached) {
            init();
        }
        return waitForPresentFenceOnDisplaySwitch;
    }

}
