package com.android.systemui;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityTransitionUseLargestWindow();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowDozeTouchesForLockIcon();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ambientCuePlugin();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ambientTouchMonitorListenToDisplayChanges();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean animationLibraryShellMigration();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean appClipsBacklinks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean avalancheReplaceHunWhenCritical();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean backButtonOnBouncer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean blockMouseEdgeBackGesture();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean blurOnMoreSurfaces();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean blurSettingsToggle();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bouncerUiRevamp();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bouncerUiRevamp2();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bpColors();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean brightnessSliderFocusState();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean captionsToggleInVolumeDialogV1();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean checkDesktopModeForSpacialModelAppPushback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean classicFlagsMultiUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cleanupInstancesWhenDisplayRemoved();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clearShortcutIconTint();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clipboardOverlayMultiuser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clipboardUseDescriptionMimetype();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clockFidgetAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean clockModernization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalBouncerDoNotModifyPluginOpen();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalEditWidgetsActivityFinishFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalHub();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalHubUseThreadPoolForWidgets();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalPowerTransitionFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalResponsiveGrid();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalShadeTouchHandlingFixes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalStandaloneSupport();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalTimerFlickerFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalWidgetResizing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean communalWidgetTrampolineFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean composeBouncer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean confineNotificationTouchToViewWidth();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean contAuthPlugin();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean coroutineTracing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean decoupleViewControllerInAnimlib();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deferDozeTransitionOnShadeDrag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean desktopAvControlsPopup();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean desktopEffectsQsTile();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean desktopSizing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dialogAnimEndStateUpdate();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableContextualTipsFrequencyCheck();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableContextualTipsIosSwitcherCheck();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableDoubleClickSwapOnBouncer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableUserSwitcherDropdownOnBouncer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean doNotUseImmediateCoroutineDispatcher();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean doNotUseRunBlocking();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean doubleTapToSleep();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamBiometricPromptFixes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamBouncerTransitionFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamInputSessionPilferOnce();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamOverlayBouncerSwipeDirectionFiltering();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamOverlayUpdatedUi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean dreamSuppression();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean edgeBackGestureHandlerThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean edgebackGestureHandlerGetRunningTasksBackground();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBackgroundKeyguardOndrawnCallback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableConstraintLayoutLockscreenOnExternalDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableContextualTipForMuteVolume();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableCueBarAnimatedIcon();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDesktopGrowth();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableEfficientDisplayRepository();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableLayoutTracing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMinmode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableOutputSwitcherAudioSharingButton();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableSuggestedDeviceUi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableUnderlay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableViewCaptureTracing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean exampleFlag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean expandCollapsePrivacyDialog();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean expandHeadsUpOnInlineReply();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean expandableUseModifierImplementation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean expandedPrivacyIndicatorsOnLargeScreen();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean extendedAppsShortcutCategory();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean faceScanningAnimationNpeFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fetchBookmarksXmlKeyboardShortcuts();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixShadeHeaderWrongIconSize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean flashlightStrength();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean floatingMenuDragToHide();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean floatingMenuHearingDeviceStatusIcon();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean floatingMenuRadiiAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean gestureBetweenHubAndLockscreenMotion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean getConnectedDeviceNameUnsynchronized();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubAllowKeyguardWhenDreaming();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubBlurredBackground();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubDirectEditMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubEnabledByDefault();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubV2();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean glanceableHubV2Resources();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean globalActionsEmphasizedFont();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hardwareColorStyles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hearingAidsQsTileDialog();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hearingDevicesDialogRelatedTools();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hideRingerButtonInSingleVolumeMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean homeControlsDreamHsum();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hsuQsChanges();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hubBlurredByShadeFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hubEditModeTouchAdjustments();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hubEditModeTransition();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean iconRefresh2025();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean indicationTextA11yFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyboardDockingIndicator();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyboardShortcutHelperRewrite();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyboardTouchpadContextualEducation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyguardTransitionForceFinishOnScreenOff();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean keyguardWmStateRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean largeScreenRecording();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean largeScreenScreencapture();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean largeScreenScreenshotAppWindow();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean largeScreenSharing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean lockscreenShadeToDreamTransitionFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean lowLightClockDream();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean lowlightClockSetBrightness();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean lowlightClockUsesKeyguardChargingStatus();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean magneticNotificationSwipes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaCarouselArrows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaControlsButtonMedia3();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaControlsButtonMedia3Placement();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaControlsInCompose();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaControlsTranslationFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaFrameDimensionsFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaProjectionDialogBehindLockscreen();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mediaProjectionGreyErrorText();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean modesUiDialogPaging();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean moveTransitionAnimationLayer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean msdlFeedback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean multiuserOpenUserSwitcherDialog();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean multiuserWifiPickerTrackerSupport();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean newAodTransition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean newDozingKeyguardStates();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean newScreenRecordToolbar();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean newVolumePanel();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean noExpansionOnOverscroll();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean noShadeBlurOnDreamStart();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean nonTouchscreenDevicesBypassFalsing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notesRoleQsTile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAddXOnHoverToDismiss();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAmbientSuppressionAfterInflation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAnimatedActionsTreatment();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAppearNonlinear();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAsyncGroupHeaderInflation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAvalancheSuppression();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationAvalancheThrottleHun();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationBackgroundTintOptimization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationBundleUi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationChildrenContainerMinHeight();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationColorUpdateLogger();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationFixHunShadows();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationFooterBackgroundTintOptimization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationRowIsRemovedFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationRowTransparency();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationShadeBlur();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationShadeUiThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationSkipSilentUpdates();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationTransparentHeaderFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationsHideOnDisplaySwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationsIconContainerRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationsRedesignFooterView();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ongoingActivityChipsOnDream();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean overrideSuppressOverlayCondition();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean permissionHelperInlineUiRichOngoing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean permissionHelperUiRichOngoing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean physicalNotificationMovement();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean pinInputFieldStyledFocusState();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean predictiveBackAnimateShade();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean privacyDotLiveRegion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean promoteNotificationsAutomatically();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean pssTaskSwitcher();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsComposeFragmentEarlyExpansion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsEditModeTooltip();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsEditModeV2();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsMaterialExpressiveTiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsNewTiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsNewTilesFuture();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsSplitInternetTile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsTileDetailedView();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsTileFocusState();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsTileTransitionInteractionRefinement();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsUiRefactorComposeFragment();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean qsWifiConfig();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean recordIssueQsTile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean redesignMagnificationWindowSize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean registerWallpaperNotifierBackground();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeDreamOverlayHideOnTouch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeNearbyShareTileAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeUpdateListenerInQsIconViewImpl();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean resetTilesRemovesCustomTiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restToUnlock();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restartDreamOnUnocclude();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restoreShowTapsSetting();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restrictCommunalAppWidgetHostListening();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restrictCommunalShadeToWhenIdle();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean revampedBouncerMessages();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean runFingerprintDetectOnDismissibleKeyguard();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sceneContainer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenOffAnimationGuardEnabled();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenReactions();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshareNotificationHidingBugFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotAnnounceLiveRegion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotDismissalSpring();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotForceShutterSound();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotMultidisplayFocusChange();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotPolicySplitAndDesktopMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean screenshotScrollCropViewCrashFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scrimFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean secondaryUserWidgetHost();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean settingsExtRegisterContentObserverOnBgThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeAppLaunchAnimationSkipInDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeExpandsOnStatusBarLongPress();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeHeaderBlurFontColor();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeHeaderFontUpdate();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeQsvisibleLogic();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shadeWindowGoesAround();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shaderlibLoadingEffectRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shortcutHelperKeyGlyph();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean shortcutHelperMultiDisplaySupport();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showAudioSharingSliderInVolumePanel();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showClipboardIndication();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showIconInEmptyShade();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean showLockedByYourWatchKeyguardIndicator();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean signOutButtonOnKeyguardStatusBar();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean simPinBouncerReset();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sliceManagerBinderCallBackground();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean smartspaceRelocateToBottom();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean smartspaceSwipeEventLoggingFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean smartspaceViewpager2();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sounddoseCustomization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean spatialModelAppPushback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean spatialModelBouncerPushback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean spatialModelPushbackInShader();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean stabilizeHeadsUpGroupV2();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarAlwaysCheckUnderlyingNetworks();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarAlwaysScheduleAutoHide();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarAlwaysUseRegionSampling();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarAppHandleTracking();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarBatteryNoConflation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarCallChipUseIsHidden();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarChipToHunAnimation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarChipsModernization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarChipsReturnAnimations();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarDarkIconInteractorMixedFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarDate();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarFontUpdates();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarForDesktop();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarMobileIconKairos();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarNoHunBehavior();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarPopupChips();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarPrivacyChipAnimationExemption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarRegionSampling();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarRootModernization();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarRudimentaryBattery();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarShareDialogWithAppName();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarShowIconsInSecureCamera();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarStaticInoutIndicators();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarSwitchToSpnFromDataSpn();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarSystemStatusIconsInCompose();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarUiThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarUniversalBatteryDataSource();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean stuckHearingDevicesQsTileFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean switchUserOnBg();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sysuiIntrinsicLockDispatcher();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean sysuiTeamfood();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean themeOverlayControllerWakefulnessDeprecation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean thinScreenRecordingService();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean unfoldAnimationBackgroundProgress();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateKeyguardOnWakeAndUnlockEarlier();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateUserSwitcherBackground();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateWindowMagnifierBottomBoundaryWithMouse();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useAadProxSensorIfPresent();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean userEncryptedSource();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean userSwitcherAddSignOutOption();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean visualInterruptionsRefactor();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean volumeRedesign();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean windowMagnificationMoveWithMouseOnEdge();
}
