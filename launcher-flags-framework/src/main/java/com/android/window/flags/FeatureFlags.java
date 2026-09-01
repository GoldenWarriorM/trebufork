package com.android.window.flags;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityEmbeddingAbortCrossUidLaunchInFinishingTaskFragment();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityEmbeddingDelayTaskFragmentFinishForActivityLaunch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityEmbeddingInteractiveDividerFlag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityEmbeddingMetrics();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowDisableActivityRecordInputSink();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean alwaysDrawMagnificationFullscreenBorder();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean alwaysSeqIdLayout();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean alwaysSeqIdLayoutWear();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean alwaysUpdateWallpaperPermission();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean aodTransition();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatPropertiesApi();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoring();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringFixMultiwindowTaskHierarchy();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringForceChangeForLetterboxTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringRoundedCorners();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringRoundedCornersAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringSetAppboundsToNullWhenEmpty();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringSkipStartingWindowLetterbox();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatRefactoringUseActivityLeashForLetterboxing();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appCompatUiFramework();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appHandleNoRelayoutOnExclusionChange();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean applyDeskActivationOnUserSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean applyLifecycleOnPipChange();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean avoidRebindingIntentionallyDisconnectedWallpaper();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean backupAndRestoreForUserAspectRatioSettings();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balAdditionalStartModes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balCheckBroadcastWhenDispatched();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balCoverIntentSender();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balDontBringExistingBackgroundTaskStackToFg();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balReduceGracePeriod();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balReportAbortedActivityStarts();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balRespectAppSwitchStateWhenCheckBoundByForegroundUid();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balSendIntentWithOptions();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean balStrictModeRo();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cameraCompatFullscreenPickSameTaskActivity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cameraCompatLandscapeCameraSupport();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cameraCompatUnifyCameraPolicies();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clearReusableScvhOnRelease();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean closeFullscreenAndSplitscreenKeyboardShortcut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean closeTaskKeyboardShortcut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean closeToSquareConfigIncludesStatusBar();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean coverDisplayOptIn();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean currentAnimatorScaleUsesSharedMemory();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean defaultDeskWithoutWarmupMigration();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deferResumeFocusInNonFocusedWindow();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean density390Api();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deprecateSurfaceAnimationFrameCallback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deprecateWindowAnimatorFrameCallback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dimmingWallpaperForMaximizedAndTiled();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableDesktopLaunchParamsOutsideDesktopBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableNonResizableAppSnapResizing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableRestoreNonFullscreenBoundsOnConfigurationChange();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean doNotForceWallpaperForFreeformTask();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAccessibleCustomHeaders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableActivityEmbeddingSupportForConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAppHandlePositionReporting();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAppHeaderWithTaskDensity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAppToWebEducationAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAutoRestartOnDisplayMove();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBackupAndRestoreDisplayWindowSettings();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBlockNonDesktopDisplayWindowDragBugfix();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBorderSettings();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBoundsRestoringOnTilingExit();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBoxShadowSettings();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBugFixesForSecondaryDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatCompatibilityInfoRotateAndCropBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatExternalDisplayRotationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatForDesktopWindowing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatForDesktopWindowingOptOut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatForDesktopWindowingOptOutApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatSandboxDisplayRotationOnExternalDisplaysBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCameraCompatTrackTaskAndAppBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCaptionCompatInsetConversion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCaptionCompatInsetForceConsumption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCaptionCompatInsetForceConsumptionAlways();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCascadingWindows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCloseLidInteraction();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCompatUiDesktopModeSynchronizationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCompatUiVisibilityStatus();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCompatuiSysuiLauncherFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableConnectedDisplaysDnd();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableConnectedDisplaysPip();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableConnectedDisplaysWallpaperPresentations();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableConnectedDisplaysWindowDrag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCrashLoggingForDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCrossDisplaysAppLaunchTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCrossDisplaysPipTaskLaunch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppHandleAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppHeaderStateChangeAnnouncements();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppLaunchAlttabTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppLaunchAlttabTransitionsBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppLaunchBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppLaunchTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopAppLaunchTransitionsBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopCloseShortcutBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopCloseTaskAnimationInDtcBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstBasedDefaultToDesktopBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstBasedDragToMaximize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstFullscreenRefocusBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstListener();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstPolicyInLpm();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopFirstTopFullscreenBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopImeBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopImmersiveDragBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopIndicatorInSeparateThreadBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopInvisibleTaskRemovalCleanupBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopModeThroughDevOption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopOpeningDeeplinkMinimizeAnimationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopRecentsTransitionsCornersBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopSplitscreenTransitionBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopSystemDialogsTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopTabTearingLaunchAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopTabTearingMinimizeAnimationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopTaskLimitSeparateTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopTaskbarOnFreeformDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopTrampolineCloseAnimationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWallpaperActivityForSystemUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingAppHandleEducation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingAppToWeb();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingAppToWebEducation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingAppToWebEducationIntegration();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingBackNavigation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingEnterTransitionBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingEnterTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingEnterpriseBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingExitByMinimizeTransitionBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingExitTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingExitTransitionsBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingHsum();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingImmersiveHandleHiding();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingModalsPolicy();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingMultiInstanceFeatures();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingPersistence();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingPip();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingPipInOverviewBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingQuickSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingScvhCacheBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingSizeConstraints();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingTaskLimit();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingTaskbarRunningApps();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopWindowingWallpaperActivity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDeviceStateAutoRotateSettingLogging();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDeviceStateAutoRotateSettingRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDialogDisplayFixes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDisplayCompatMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDisplayDisconnectInteraction();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDisplayFocusInShellTransitions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDisplayReconnectInteraction();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDisplayWindowingModeSwitching();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDragEndStableBoundsReset();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDragResizeSetUpInBgThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDragToDesktopIncomingTransitionsBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDragToMaximize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDraggingPipAcrossDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDrawingAppHandle();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDreamActivityWindowingExclusion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDynamicRadiusComputationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableEmptyDeskOnMinimize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableExperimentalBubblesController();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableExternalDisplayPersistenceBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableFreeformBoxShadows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableFreeformDisplayLaunchParams();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableFullScreenWindowOnRemovingSplitScreenStageBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableFullscreenWindowControls();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableFullyImmersiveInDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableHandleInputFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableHandlersDebuggingMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableHoldToDragAppHandle();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableIndependentBackInProjected();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableInorderTransitionCallbacksForDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableInputLayerTransitionFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableInteractionDependentTabTearingBounds();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableInteractivePictureInPicture();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableKeyGestureHandlerForSysui();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableLauncherHandleGoHomeKeyboardShortcut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMinimizeButton();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMirrorDisplayNoActivity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableModalsFullscreenWithPermission();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableModalsFullscreenWithPlatformSignature();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMoveToNextDisplayShortcut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultiDisplayHomeFocusBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultiDisplaySplit();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultidisplayTrackpadBackGesture();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultipleDesktopsBackend();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultipleDesktopsDefaultActivationInDesktopFirstDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMultipleDesktopsFrontend();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableNoWindowDecorationForDesks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableNonDefaultDisplaySplit();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableNonDefaultDisplaySplitBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableOpaqueBackgroundForTransparentWindows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableOverflowButtonForTaskbarPinnedItems();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePerDisplayDesktopWallpaperActivity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePerDisplayPackageContextCacheInStatusbarNotif();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePerDisplayWindowDecorViewHostPool();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePersistingDisplaySizeForConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePinningAppWithContextMenu();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePipParamsUpdateNotificationBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePresentationDisallowedOnUnfocusedHostTask();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePresentationForConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableProjectedDisplayDesktopMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableQuickswitchDesktopSplitBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRejectHomeTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRemoveStatusBarInputLayer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRequestFullscreenBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRequestFullscreenRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRequestFullscreenRestoreFreeformBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableResizingMetrics();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRestartMenuForConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRestoreToPreviousSizeFromDesktopImmersive();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableSeeThroughTaskFragments();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableShellInitialBoundsRegressionBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableShrinkWindowBoundsAfterDrag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableSizeCompatModeImprovementsForConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableStartLaunchTransitionFromTaskbarBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableSysDecorsCallbacksViaWm();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTallAppHeaders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskResizingKeyboardShortcuts();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskStackObserverInShell();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskbarConnectedDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskbarOverflow();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskbarRecentTasksThrottleBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskbarRecentsLayoutTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableThemedAppHeaders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTileResizing();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTopVisibleRootTaskPerUserTracking();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTransitionOnActivitySetRequestedOrientation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableUpdatedDisplayConnectionDialog();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableUpscalingSizeCompatOnExitingDesktopBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableVisualIndicatorInTransitionBugfix();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowContextResourcesUpdateOnConfigChange();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowDecorationRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowDropSmoothTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowRepositioningApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowingDynamicInitialBounds();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowingEdgeDragResize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowingScaledResizing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowingTaskStackOrderBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableWindowingTransitionHandlersObservers();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enforceEdgeToEdge();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ensureKeyguardDoesTransitionStartingBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ensureWallpaperDrawnOnDisplaySwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enterDesktopByDefaultOnFreeformDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean excludeCaptionFromAppBounds();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean excludeDeskRootsFromDesktopTasks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean excludingLayerFromTaskSnapshot();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fallbackToFocusedDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fallbackTransitionPlayer();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fifoPriorityForMajorUiProcesses();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixBalReparentExistingTask();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixBubbleTrampolineAnimation();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixHideOverlayApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixLeakingVisualIndicator();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixRapidTopResumedSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean forceCloseTopTransparentFullscreenTask();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean formFactorBasedDesktopFirstSwitch();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean grantManageKeyGesturesToRecents();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean handleIncompatibleTasksInDesktopLaunchParams();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ignoreAspectRatioRestrictionsForResizeableFreeformActivities();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ignoreCornerRadiusAndShadows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ignoreCurrentParamsInDesktopLaunchParams();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ignoreOverrideTaskBoundsIfIncompatibleWithDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean imeBackCallbackLeakPrevention();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean includeTopTransparentFullscreenTaskInDesktopHeuristic();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean inheritTaskBoundsForTrampolineTaskLaunches();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean jankApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyboardShortcutsToSwitchDesks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyguardRemoveDefaultDisplayUsage();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean letterboxBackgroundWallpaper();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean limitSystemFullscreenOverrideToDefaultDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean migrateBasicLegacyReady();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean moveToExternalDisplayShortcut();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean moveToNextDisplayShortcutWithProjectedMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean multiCrop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean multipleSystemNavigationObserverCallbacks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean nestedTasksWithIndependentBoundsBugfix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean noAlphaRotationEnterAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean optOutOverrideOrientationToUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean parallelCdTransitionsDuringRecents();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean polishCloseWallpaperIncludesOpenChange();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean portWindowSizeAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackCallbackCancellationFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackDelayWmTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackInterceptTransition();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackPrioritySystemNavigationObserver();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackStopKeycodeBackForwarding();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackSwipeEdgeNoneApi();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackSystemOverrideCallback();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackTimestampApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean preserveRecentsTaskConfigurationOnRelaunch();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean rearDisplayDisableForceDesktopSystemDecorations();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reduceChangedExclusionRectsMsgs();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reduceTaskSnapshotMemoryUsage();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reenableAppHandleAnimations();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reenableAppHandleColorAnimations();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean refactorMatchParentBounds();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean relativeInsets();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean releaseAllTransitionSurfaces();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeDeskOnLastTaskRemoval();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeGetDimmer();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reparentWindowTokenApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean repositoryBasedPersistence();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean repositoryBasedPersistenceBgThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean respectFullscreenActivityOptionInDesktopLaunchParams();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean respectOrientationChangeForUnresizeable();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean respectRequestedTaskSnapshotResolution();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restoreUserAspectRatioSettingsUsingService();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restrictFreeformHiddenSystemBarsToFillingTasks();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean returnAllVisibleActivitiesForVis();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean rootTaskForBubble();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean safeRegionLetterboxingV1();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenRecordingCallbacks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scrollingFromLetterbox();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scvhSetFocusable();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scvhSurfaceControlLifetimeFix();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sdkDesiredPresentTime();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean setScPropertiesInClient();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showAppHandleLargeScreens();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showBiometricPromptSecondaryDisplayMessage();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showDesktopExperienceDevOption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showDesktopWindowingDevOption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showHomeBehindDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean skipCompatUiEducationInDesktopMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean skipDeactivationOfDeskWithNothingInFront();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean skipDecorViewRelayoutWhenClosingBugfix();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean supportGeminiOnMultiDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean systemContentPriority();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean systemUiPostAnimationEnd();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean taskFragmentCompanionActivity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean taskbarRunningTasksInSplitscreenSelect();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean toggleFullscreenStateViaFullscreenKey();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean touchPassThroughOptIn();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean transferStartingWindowToNextWhenInvisible();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean transitReadyTracking();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean transitTrackerPlumbing();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean transitionHandlerCujTags();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean unifyShellBinders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean universalResizableByDefault();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean untrustedEmbeddingAnyAppPermission();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean untrustedEmbeddingStateSharing();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateDimsWhenWindowShown();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateTaskCropInSync();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useInputReportedFocusForAccessibility();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean vdmForceAppUniversalResizableApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean waitForPresentFenceOnDisplaySwitch();
}
