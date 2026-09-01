package com.android.systemui;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean activityTransitionUseLargestWindow = true;
    private static boolean allowDozeTouchesForLockIcon = false;
    private static boolean ambientCuePlugin = true;
    private static boolean ambientTouchMonitorListenToDisplayChanges = false;
    private static boolean animationLibraryShellMigration = false;
    private static boolean appClipsBacklinks = true;
    private static boolean avalancheReplaceHunWhenCritical = true;
    private static boolean backButtonOnBouncer = false;
    private static boolean blockMouseEdgeBackGesture = true;
    private static boolean blurOnMoreSurfaces = false;
    private static boolean blurSettingsToggle = true;
    private static boolean bouncerUiRevamp = true;
    private static boolean bouncerUiRevamp2 = true;
    private static boolean bpColors = true;
    private static boolean brightnessSliderFocusState = false;
    private static boolean captionsToggleInVolumeDialogV1 = true;
    private static boolean checkDesktopModeForSpacialModelAppPushback = false;
    private static boolean classicFlagsMultiUser = true;
    private static boolean cleanupInstancesWhenDisplayRemoved = true;
    private static boolean clearShortcutIconTint = true;
    private static boolean clipboardOverlayMultiuser = true;
    private static boolean clipboardUseDescriptionMimetype = true;
    private static boolean clockFidgetAnimation = true;
    private static boolean clockModernization = false;
    private static boolean communalBouncerDoNotModifyPluginOpen = true;
    private static boolean communalEditWidgetsActivityFinishFix = true;
    private static boolean communalHub = true;
    private static boolean communalHubUseThreadPoolForWidgets = true;
    private static boolean communalPowerTransitionFix = true;
    private static boolean communalResponsiveGrid = true;
    private static boolean communalShadeTouchHandlingFixes = true;
    private static boolean communalStandaloneSupport = false;
    private static boolean communalTimerFlickerFix = true;
    private static boolean communalWidgetResizing = true;
    private static boolean communalWidgetTrampolineFix = true;
    private static boolean composeBouncer = false;
    private static boolean confineNotificationTouchToViewWidth = true;
    private static boolean contAuthPlugin = true;
    private static boolean coroutineTracing = true;
    private static boolean decoupleViewControllerInAnimlib = false;
    private static boolean deferDozeTransitionOnShadeDrag = false;
    private static boolean desktopAvControlsPopup = false;
    private static boolean desktopEffectsQsTile = false;
    private static boolean dialogAnimEndStateUpdate = true;
    private static boolean disableContextualTipsFrequencyCheck = false;
    private static boolean disableContextualTipsIosSwitcherCheck = true;
    private static boolean disableDoubleClickSwapOnBouncer = true;
    private static boolean disableUserSwitcherDropdownOnBouncer = false;
    private static boolean doNotUseImmediateCoroutineDispatcher = false;
    private static boolean doNotUseRunBlocking = false;
    private static boolean doubleTapToSleep = true;
    private static boolean dreamBiometricPromptFixes = true;
    private static boolean dreamBouncerTransitionFix = true;
    private static boolean dreamInputSessionPilferOnce = true;
    private static boolean dreamOverlayBouncerSwipeDirectionFiltering = true;
    private static boolean dreamSuppression = true;
    private static boolean edgeBackGestureHandlerThread = false;
    private static boolean edgebackGestureHandlerGetRunningTasksBackground = true;
    private static boolean enableBackgroundKeyguardOndrawnCallback = true;
    private static boolean enableConstraintLayoutLockscreenOnExternalDisplay = true;
    private static boolean enableContextualTipForMuteVolume = false;
    private static boolean enableCueBarAnimatedIcon = false;
    private static boolean enableDesktopGrowth = true;
    private static boolean enableEfficientDisplayRepository = false;
    private static boolean enableLayoutTracing = false;
    private static boolean enableMinmode = true;
    private static boolean enableOutputSwitcherAudioSharingButton = true;
    private static boolean enableSuggestedDeviceUi = true;
    private static boolean enableUnderlay = true;
    private static boolean enableViewCaptureTracing = true;
    private static boolean exampleFlag = false;
    private static boolean expandCollapsePrivacyDialog = true;
    private static boolean expandHeadsUpOnInlineReply = true;
    private static boolean expandableUseModifierImplementation = true;
    private static boolean expandedPrivacyIndicatorsOnLargeScreen = false;
    private static boolean extendedAppsShortcutCategory = true;
    private static boolean faceScanningAnimationNpeFix = true;
    private static boolean fetchBookmarksXmlKeyboardShortcuts = true;
    private static boolean fixShadeHeaderWrongIconSize = true;
    private static boolean flashlightStrength = true;
    private static boolean floatingMenuDragToHide = false;
    private static boolean floatingMenuHearingDeviceStatusIcon = false;
    private static boolean floatingMenuRadiiAnimation = true;
    private static boolean gestureBetweenHubAndLockscreenMotion = true;
    private static boolean getConnectedDeviceNameUnsynchronized = true;
    private static boolean glanceableHubAllowKeyguardWhenDreaming = false;
    private static boolean glanceableHubBlurredBackground = true;
    private static boolean glanceableHubDirectEditMode = true;
    private static boolean glanceableHubEnabledByDefault = true;
    private static boolean glanceableHubV2 = true;
    private static boolean globalActionsEmphasizedFont = true;
    private static boolean hardwareColorStyles = true;
    private static boolean hearingAidsQsTileDialog = true;
    private static boolean hearingDevicesDialogRelatedTools = true;
    private static boolean hideRingerButtonInSingleVolumeMode = true;
    private static boolean homeControlsDreamHsum = true;
    private static boolean hsuQsChanges = true;
    private static boolean hubBlurredByShadeFix = true;
    private static boolean hubEditModeTouchAdjustments = true;
    private static boolean hubEditModeTransition = true;
    private static boolean indicationTextA11yFix = true;
    private static boolean keyboardDockingIndicator = false;
    private static boolean keyboardShortcutHelperRewrite = true;
    private static boolean keyboardTouchpadContextualEducation = true;
    private static boolean keyguardTransitionForceFinishOnScreenOff = true;
    private static boolean keyguardWmStateRefactor = false;
    private static boolean largeScreenRecording = false;
    private static boolean largeScreenScreencapture = false;
    private static boolean largeScreenScreenshotAppWindow = false;
    private static boolean largeScreenSharing = false;
    private static boolean lockscreenShadeToDreamTransitionFix = true;
    private static boolean lowLightClockDream = false;
    private static boolean lowlightClockSetBrightness = true;
    private static boolean lowlightClockUsesKeyguardChargingStatus = true;
    private static boolean magneticNotificationSwipes = true;
    private static boolean mediaCarouselArrows = true;
    private static boolean mediaControlsButtonMedia3 = false;
    private static boolean mediaControlsButtonMedia3Placement = false;
    private static boolean mediaControlsInCompose = false;
    private static boolean mediaControlsTranslationFix = true;
    private static boolean mediaFrameDimensionsFix = true;
    private static boolean mediaProjectionDialogBehindLockscreen = true;
    private static boolean mediaProjectionGreyErrorText = true;
    private static boolean modesUiDialogPaging = false;
    private static boolean moveTransitionAnimationLayer = true;
    private static boolean msdlFeedback = true;
    private static boolean multiuserOpenUserSwitcherDialog = true;
    private static boolean multiuserWifiPickerTrackerSupport = true;
    private static boolean newAodTransition = true;
    private static boolean newDozingKeyguardStates = true;
    private static boolean newScreenRecordToolbar = false;
    private static boolean newVolumePanel = true;
    private static boolean noExpansionOnOverscroll = true;
    private static boolean noShadeBlurOnDreamStart = false;
    private static boolean nonTouchscreenDevicesBypassFalsing = false;
    private static boolean notesRoleQsTile = false;
    private static boolean notificationAddXOnHoverToDismiss = false;
    private static boolean notificationAmbientSuppressionAfterInflation = false;
    private static boolean notificationAnimatedActionsTreatment = true;
    private static boolean notificationAppearNonlinear = true;
    private static boolean notificationAsyncGroupHeaderInflation = true;
    private static boolean notificationAvalancheSuppression = true;
    private static boolean notificationAvalancheThrottleHun = true;
    private static boolean notificationBackgroundTintOptimization = false;
    private static boolean notificationBundleUi = true;
    private static boolean notificationChildrenContainerMinHeight = false;
    private static boolean notificationColorUpdateLogger = false;
    private static boolean notificationFixHunShadows = false;
    private static boolean notificationFooterBackgroundTintOptimization = false;
    private static boolean notificationRowIsRemovedFix = true;
    private static boolean notificationRowTransparency = true;
    private static boolean notificationShadeBlur = true;
    private static boolean notificationShadeUiThread = false;
    private static boolean notificationSkipSilentUpdates = true;
    private static boolean notificationTransparentHeaderFix = true;
    private static boolean notificationsHideOnDisplaySwitch = false;
    private static boolean notificationsIconContainerRefactor = true;
    private static boolean notificationsRedesignFooterView = true;
    private static boolean ongoingActivityChipsOnDream = true;
    private static boolean overrideSuppressOverlayCondition = false;
    private static boolean permissionHelperInlineUiRichOngoing = true;
    private static boolean permissionHelperUiRichOngoing = true;
    private static boolean physicalNotificationMovement = true;
    private static boolean pinInputFieldStyledFocusState = true;
    private static boolean predictiveBackAnimateShade = false;
    private static boolean privacyDotLiveRegion = false;
    private static boolean promoteNotificationsAutomatically = false;
    private static boolean pssTaskSwitcher = false;
    private static boolean qsComposeFragmentEarlyExpansion = true;
    private static boolean qsEditModeTooltip = true;
    private static boolean qsEditModeV2 = false;
    private static boolean qsMaterialExpressiveTiles = false;
    private static boolean qsNewTiles = false;
    private static boolean qsNewTilesFuture = false;
    private static boolean qsTileDetailedView = true;
    private static boolean qsTileFocusState = true;
    private static boolean qsTileTransitionInteractionRefinement = true;
    private static boolean qsUiRefactorComposeFragment = true;
    private static boolean qsWifiConfig = false;
    private static boolean recordIssueQsTile = true;
    private static boolean redesignMagnificationWindowSize = true;
    private static boolean registerWallpaperNotifierBackground = true;
    private static boolean removeDreamOverlayHideOnTouch = true;
    private static boolean removeNearbyShareTileAnimation = false;
    private static boolean removeUpdateListenerInQsIconViewImpl = true;
    private static boolean resetTilesRemovesCustomTiles = true;
    private static boolean restToUnlock = false;
    private static boolean restartDreamOnUnocclude = false;
    private static boolean restoreShowTapsSetting = true;
    private static boolean restrictCommunalAppWidgetHostListening = true;
    private static boolean restrictCommunalShadeToWhenIdle = false;
    private static boolean revampedBouncerMessages = true;
    private static boolean runFingerprintDetectOnDismissibleKeyguard = true;
    private static boolean sceneContainer = false;
    private static boolean screenOffAnimationGuardEnabled = true;
    private static boolean screenReactions = false;
    private static boolean screenshareNotificationHidingBugFix = true;
    private static boolean screenshotAnnounceLiveRegion = true;
    private static boolean screenshotDismissalSpring = false;
    private static boolean screenshotForceShutterSound = true;
    private static boolean screenshotMultidisplayFocusChange = true;
    private static boolean screenshotPolicySplitAndDesktopMode = true;
    private static boolean screenshotScrollCropViewCrashFix = true;
    private static boolean scrimFix = true;
    private static boolean secondaryUserWidgetHost = false;
    private static boolean settingsExtRegisterContentObserverOnBgThread = true;
    private static boolean shadeAppLaunchAnimationSkipInDesktop = true;
    private static boolean shadeExpandsOnStatusBarLongPress = true;
    private static boolean shadeQsvisibleLogic = false;
    private static boolean shadeWindowGoesAround = true;
    private static boolean shaderlibLoadingEffectRefactor = true;
    private static boolean shortcutHelperKeyGlyph = true;
    private static boolean shortcutHelperMultiDisplaySupport = true;
    private static boolean showAudioSharingSliderInVolumePanel = true;
    private static boolean showClipboardIndication = false;
    private static boolean showIconInEmptyShade = false;
    private static boolean showLockedByYourWatchKeyguardIndicator = true;
    private static boolean signOutButtonOnKeyguardStatusBar = true;
    private static boolean simPinBouncerReset = true;
    private static boolean sliceManagerBinderCallBackground = true;
    private static boolean smartspaceRelocateToBottom = false;
    private static boolean smartspaceSwipeEventLoggingFix = true;
    private static boolean smartspaceViewpager2 = true;
    private static boolean sounddoseCustomization = true;
    private static boolean spatialModelAppPushback = true;
    private static boolean spatialModelBouncerPushback = false;
    private static boolean spatialModelPushbackInShader = true;
    private static boolean stabilizeHeadsUpGroupV2 = true;
    private static boolean statusBarAlwaysCheckUnderlyingNetworks = true;
    private static boolean statusBarAlwaysScheduleAutoHide = true;
    private static boolean statusBarAlwaysUseRegionSampling = false;
    private static boolean statusBarAppHandleTracking = true;
    private static boolean statusBarBatteryNoConflation = true;
    private static boolean statusBarCallChipUseIsHidden = true;
    private static boolean statusBarChipToHunAnimation = false;
    private static boolean statusBarChipsModernization = true;
    private static boolean statusBarChipsReturnAnimations = false;
    private static boolean statusBarDarkIconInteractorMixedFix = true;
    private static boolean statusBarDate = false;
    private static boolean statusBarForDesktop = true;
    private static boolean statusBarMobileIconKairos = true;
    private static boolean statusBarNoHunBehavior = true;
    private static boolean statusBarPopupChips = false;
    private static boolean statusBarPrivacyChipAnimationExemption = true;
    private static boolean statusBarRegionSampling = true;
    private static boolean statusBarRootModernization = true;
    private static boolean statusBarRudimentaryBattery = false;
    private static boolean statusBarShareDialogWithAppName = true;
    private static boolean statusBarShowIconsInSecureCamera = false;
    private static boolean statusBarStaticInoutIndicators = false;
    private static boolean statusBarSwitchToSpnFromDataSpn = true;
    private static boolean statusBarSystemStatusIconsInCompose = false;
    private static boolean statusBarUiThread = false;
    private static boolean statusBarUniversalBatteryDataSource = true;
    private static boolean stuckHearingDevicesQsTileFix = true;
    private static boolean switchUserOnBg = true;
    private static boolean sysuiIntrinsicLockDispatcher = true;
    private static boolean sysuiTeamfood = true;
    private static boolean themeOverlayControllerWakefulnessDeprecation = false;
    private static boolean thinScreenRecordingService = true;
    private static boolean unfoldAnimationBackgroundProgress = true;
    private static boolean updateKeyguardOnWakeAndUnlockEarlier = true;
    private static boolean updateUserSwitcherBackground = true;
    private static boolean updateWindowMagnifierBottomBoundaryWithMouse = true;
    private static boolean userEncryptedSource = true;
    private static boolean userSwitcherAddSignOutOption = false;
    private static boolean visualInterruptionsRefactor = true;
    private static boolean volumeRedesign = true;
    private static boolean windowMagnificationMoveWithMouseOnEdge = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("com.android.systemui", 0x6FDC0EBD871C2257L);
            cleanupInstancesWhenDisplayRemoved = reader.getBooleanFlagValue(18);
            floatingMenuDragToHide = reader.getBooleanFlagValue(81);
            floatingMenuHearingDeviceStatusIcon = reader.getBooleanFlagValue(82);
            floatingMenuRadiiAnimation = reader.getBooleanFlagValue(83);
            hearingDevicesDialogRelatedTools = reader.getBooleanFlagValue(95);
            privacyDotLiveRegion = reader.getBooleanFlagValue(168);
            redesignMagnificationWindowSize = reader.getBooleanFlagValue(183);
            statusBarRegionSampling = reader.getBooleanFlagValue(250);
            updateWindowMagnifierBottomBoundaryWithMouse = reader.getBooleanFlagValue(269);
            windowMagnificationMoveWithMouseOnEdge = reader.getBooleanFlagValue(275);
            ambientCuePlugin = reader.getBooleanFlagValue(2);
            enableCueBarAnimatedIcon = reader.getBooleanFlagValue(62);
            enableUnderlay = reader.getBooleanFlagValue(69);
            bpColors = reader.getBooleanFlagValue(13);
            contAuthPlugin = reader.getBooleanFlagValue(37);
            communalHub = reader.getBooleanFlagValue(26);
            enableOutputSwitcherAudioSharingButton = reader.getBooleanFlagValue(67);
            showAudioSharingSliderInVolumePanel = reader.getBooleanFlagValue(218);
            qsWifiConfig = reader.getBooleanFlagValue(181);
            backButtonOnBouncer = reader.getBooleanFlagValue(7);
            disableDoubleClickSwapOnBouncer = reader.getBooleanFlagValue(46);
            disableUserSwitcherDropdownOnBouncer = reader.getBooleanFlagValue(47);
            signOutButtonOnKeyguardStatusBar = reader.getBooleanFlagValue(222);
            userSwitcherAddSignOutOption = reader.getBooleanFlagValue(272);
            fixShadeHeaderWrongIconSize = reader.getBooleanFlagValue(79);
            activityTransitionUseLargestWindow = reader.getBooleanFlagValue(0);
            allowDozeTouchesForLockIcon = reader.getBooleanFlagValue(1);
            ambientTouchMonitorListenToDisplayChanges = reader.getBooleanFlagValue(3);
            animationLibraryShellMigration = reader.getBooleanFlagValue(4);
            appClipsBacklinks = reader.getBooleanFlagValue(5);
            avalancheReplaceHunWhenCritical = reader.getBooleanFlagValue(6);
            blockMouseEdgeBackGesture = reader.getBooleanFlagValue(8);
            blurOnMoreSurfaces = reader.getBooleanFlagValue(9);
            blurSettingsToggle = reader.getBooleanFlagValue(10);
            bouncerUiRevamp = reader.getBooleanFlagValue(11);
            bouncerUiRevamp2 = reader.getBooleanFlagValue(12);
            brightnessSliderFocusState = reader.getBooleanFlagValue(14);
            captionsToggleInVolumeDialogV1 = reader.getBooleanFlagValue(15);
            checkDesktopModeForSpacialModelAppPushback = reader.getBooleanFlagValue(16);
            classicFlagsMultiUser = reader.getBooleanFlagValue(17);
            clearShortcutIconTint = reader.getBooleanFlagValue(19);
            clipboardOverlayMultiuser = reader.getBooleanFlagValue(20);
            clipboardUseDescriptionMimetype = reader.getBooleanFlagValue(21);
            clockFidgetAnimation = reader.getBooleanFlagValue(22);
            clockModernization = reader.getBooleanFlagValue(23);
            communalBouncerDoNotModifyPluginOpen = reader.getBooleanFlagValue(24);
            communalEditWidgetsActivityFinishFix = reader.getBooleanFlagValue(25);
            communalHubUseThreadPoolForWidgets = reader.getBooleanFlagValue(27);
            communalPowerTransitionFix = reader.getBooleanFlagValue(28);
            communalResponsiveGrid = reader.getBooleanFlagValue(29);
            communalShadeTouchHandlingFixes = reader.getBooleanFlagValue(30);
            communalStandaloneSupport = reader.getBooleanFlagValue(31);
            communalTimerFlickerFix = reader.getBooleanFlagValue(32);
            communalWidgetResizing = reader.getBooleanFlagValue(33);
            communalWidgetTrampolineFix = reader.getBooleanFlagValue(34);
            composeBouncer = reader.getBooleanFlagValue(35);
            confineNotificationTouchToViewWidth = reader.getBooleanFlagValue(36);
            coroutineTracing = reader.getBooleanFlagValue(38);
            decoupleViewControllerInAnimlib = reader.getBooleanFlagValue(39);
            deferDozeTransitionOnShadeDrag = reader.getBooleanFlagValue(40);
            desktopAvControlsPopup = reader.getBooleanFlagValue(41);
            desktopEffectsQsTile = reader.getBooleanFlagValue(42);
            dialogAnimEndStateUpdate = reader.getBooleanFlagValue(43);
            disableContextualTipsFrequencyCheck = reader.getBooleanFlagValue(44);
            disableContextualTipsIosSwitcherCheck = reader.getBooleanFlagValue(45);
            doNotUseImmediateCoroutineDispatcher = reader.getBooleanFlagValue(48);
            doNotUseRunBlocking = reader.getBooleanFlagValue(49);
            doubleTapToSleep = reader.getBooleanFlagValue(50);
            dreamBiometricPromptFixes = reader.getBooleanFlagValue(51);
            dreamBouncerTransitionFix = reader.getBooleanFlagValue(52);
            dreamInputSessionPilferOnce = reader.getBooleanFlagValue(53);
            dreamOverlayBouncerSwipeDirectionFiltering = reader.getBooleanFlagValue(54);
            dreamSuppression = reader.getBooleanFlagValue(56);
            edgeBackGestureHandlerThread = reader.getBooleanFlagValue(57);
            edgebackGestureHandlerGetRunningTasksBackground = reader.getBooleanFlagValue(58);
            enableBackgroundKeyguardOndrawnCallback = reader.getBooleanFlagValue(59);
            enableConstraintLayoutLockscreenOnExternalDisplay = reader.getBooleanFlagValue(60);
            enableContextualTipForMuteVolume = reader.getBooleanFlagValue(61);
            enableDesktopGrowth = reader.getBooleanFlagValue(63);
            enableEfficientDisplayRepository = reader.getBooleanFlagValue(64);
            enableLayoutTracing = reader.getBooleanFlagValue(65);
            enableMinmode = reader.getBooleanFlagValue(66);
            enableSuggestedDeviceUi = reader.getBooleanFlagValue(68);
            enableViewCaptureTracing = reader.getBooleanFlagValue(70);
            exampleFlag = reader.getBooleanFlagValue(71);
            expandCollapsePrivacyDialog = reader.getBooleanFlagValue(72);
            expandHeadsUpOnInlineReply = reader.getBooleanFlagValue(73);
            expandableUseModifierImplementation = reader.getBooleanFlagValue(74);
            expandedPrivacyIndicatorsOnLargeScreen = reader.getBooleanFlagValue(75);
            extendedAppsShortcutCategory = reader.getBooleanFlagValue(76);
            faceScanningAnimationNpeFix = reader.getBooleanFlagValue(77);
            fetchBookmarksXmlKeyboardShortcuts = reader.getBooleanFlagValue(78);
            flashlightStrength = reader.getBooleanFlagValue(80);
            gestureBetweenHubAndLockscreenMotion = reader.getBooleanFlagValue(84);
            getConnectedDeviceNameUnsynchronized = reader.getBooleanFlagValue(85);
            glanceableHubAllowKeyguardWhenDreaming = reader.getBooleanFlagValue(86);
            glanceableHubBlurredBackground = reader.getBooleanFlagValue(87);
            glanceableHubDirectEditMode = reader.getBooleanFlagValue(88);
            glanceableHubEnabledByDefault = reader.getBooleanFlagValue(89);
            glanceableHubV2 = reader.getBooleanFlagValue(90);
            globalActionsEmphasizedFont = reader.getBooleanFlagValue(92);
            hardwareColorStyles = reader.getBooleanFlagValue(93);
            hearingAidsQsTileDialog = reader.getBooleanFlagValue(94);
            hideRingerButtonInSingleVolumeMode = reader.getBooleanFlagValue(96);
            homeControlsDreamHsum = reader.getBooleanFlagValue(97);
            hsuQsChanges = reader.getBooleanFlagValue(98);
            hubBlurredByShadeFix = reader.getBooleanFlagValue(99);
            hubEditModeTouchAdjustments = reader.getBooleanFlagValue(100);
            hubEditModeTransition = reader.getBooleanFlagValue(101);
            indicationTextA11yFix = reader.getBooleanFlagValue(103);
            keyboardDockingIndicator = reader.getBooleanFlagValue(104);
            keyboardShortcutHelperRewrite = reader.getBooleanFlagValue(105);
            keyboardTouchpadContextualEducation = reader.getBooleanFlagValue(106);
            keyguardTransitionForceFinishOnScreenOff = reader.getBooleanFlagValue(107);
            keyguardWmStateRefactor = reader.getBooleanFlagValue(108);
            largeScreenRecording = reader.getBooleanFlagValue(109);
            largeScreenScreencapture = reader.getBooleanFlagValue(110);
            largeScreenScreenshotAppWindow = reader.getBooleanFlagValue(111);
            largeScreenSharing = reader.getBooleanFlagValue(112);
            lockscreenShadeToDreamTransitionFix = reader.getBooleanFlagValue(113);
            lowLightClockDream = reader.getBooleanFlagValue(114);
            lowlightClockSetBrightness = reader.getBooleanFlagValue(115);
            lowlightClockUsesKeyguardChargingStatus = reader.getBooleanFlagValue(116);
            magneticNotificationSwipes = reader.getBooleanFlagValue(117);
            mediaCarouselArrows = reader.getBooleanFlagValue(118);
            mediaControlsButtonMedia3 = reader.getBooleanFlagValue(119);
            mediaControlsButtonMedia3Placement = reader.getBooleanFlagValue(120);
            mediaControlsInCompose = reader.getBooleanFlagValue(121);
            mediaControlsTranslationFix = reader.getBooleanFlagValue(122);
            mediaFrameDimensionsFix = reader.getBooleanFlagValue(123);
            mediaProjectionDialogBehindLockscreen = reader.getBooleanFlagValue(124);
            mediaProjectionGreyErrorText = reader.getBooleanFlagValue(125);
            modesUiDialogPaging = reader.getBooleanFlagValue(126);
            moveTransitionAnimationLayer = reader.getBooleanFlagValue(127);
            msdlFeedback = reader.getBooleanFlagValue(128);
            multiuserOpenUserSwitcherDialog = reader.getBooleanFlagValue(129);
            multiuserWifiPickerTrackerSupport = reader.getBooleanFlagValue(130);
            newAodTransition = reader.getBooleanFlagValue(131);
            newDozingKeyguardStates = reader.getBooleanFlagValue(132);
            newScreenRecordToolbar = reader.getBooleanFlagValue(133);
            newVolumePanel = reader.getBooleanFlagValue(134);
            noExpansionOnOverscroll = reader.getBooleanFlagValue(135);
            noShadeBlurOnDreamStart = reader.getBooleanFlagValue(136);
            nonTouchscreenDevicesBypassFalsing = reader.getBooleanFlagValue(137);
            notesRoleQsTile = reader.getBooleanFlagValue(138);
            notificationAddXOnHoverToDismiss = reader.getBooleanFlagValue(139);
            notificationAmbientSuppressionAfterInflation = reader.getBooleanFlagValue(140);
            notificationAnimatedActionsTreatment = reader.getBooleanFlagValue(141);
            notificationAppearNonlinear = reader.getBooleanFlagValue(142);
            notificationAsyncGroupHeaderInflation = reader.getBooleanFlagValue(143);
            notificationAvalancheSuppression = reader.getBooleanFlagValue(144);
            notificationAvalancheThrottleHun = reader.getBooleanFlagValue(145);
            notificationBackgroundTintOptimization = reader.getBooleanFlagValue(146);
            notificationBundleUi = reader.getBooleanFlagValue(147);
            notificationChildrenContainerMinHeight = reader.getBooleanFlagValue(148);
            notificationColorUpdateLogger = reader.getBooleanFlagValue(149);
            notificationFixHunShadows = reader.getBooleanFlagValue(150);
            notificationFooterBackgroundTintOptimization = reader.getBooleanFlagValue(151);
            notificationRowIsRemovedFix = reader.getBooleanFlagValue(152);
            notificationRowTransparency = reader.getBooleanFlagValue(153);
            notificationShadeBlur = reader.getBooleanFlagValue(154);
            notificationShadeUiThread = reader.getBooleanFlagValue(155);
            notificationSkipSilentUpdates = reader.getBooleanFlagValue(156);
            notificationTransparentHeaderFix = reader.getBooleanFlagValue(157);
            notificationsHideOnDisplaySwitch = reader.getBooleanFlagValue(158);
            notificationsIconContainerRefactor = reader.getBooleanFlagValue(159);
            notificationsRedesignFooterView = reader.getBooleanFlagValue(160);
            ongoingActivityChipsOnDream = reader.getBooleanFlagValue(161);
            overrideSuppressOverlayCondition = reader.getBooleanFlagValue(162);
            permissionHelperInlineUiRichOngoing = reader.getBooleanFlagValue(163);
            permissionHelperUiRichOngoing = reader.getBooleanFlagValue(164);
            physicalNotificationMovement = reader.getBooleanFlagValue(165);
            pinInputFieldStyledFocusState = reader.getBooleanFlagValue(166);
            predictiveBackAnimateShade = reader.getBooleanFlagValue(167);
            promoteNotificationsAutomatically = reader.getBooleanFlagValue(169);
            pssTaskSwitcher = reader.getBooleanFlagValue(170);
            qsComposeFragmentEarlyExpansion = reader.getBooleanFlagValue(171);
            qsEditModeTooltip = reader.getBooleanFlagValue(172);
            qsEditModeV2 = reader.getBooleanFlagValue(173);
            qsMaterialExpressiveTiles = reader.getBooleanFlagValue(174);
            qsNewTiles = reader.getBooleanFlagValue(175);
            qsNewTilesFuture = reader.getBooleanFlagValue(176);
            qsTileDetailedView = reader.getBooleanFlagValue(177);
            qsTileFocusState = reader.getBooleanFlagValue(178);
            qsTileTransitionInteractionRefinement = reader.getBooleanFlagValue(179);
            qsUiRefactorComposeFragment = reader.getBooleanFlagValue(180);
            recordIssueQsTile = reader.getBooleanFlagValue(182);
            registerWallpaperNotifierBackground = reader.getBooleanFlagValue(184);
            removeDreamOverlayHideOnTouch = reader.getBooleanFlagValue(185);
            removeNearbyShareTileAnimation = reader.getBooleanFlagValue(186);
            removeUpdateListenerInQsIconViewImpl = reader.getBooleanFlagValue(187);
            resetTilesRemovesCustomTiles = reader.getBooleanFlagValue(188);
            restToUnlock = reader.getBooleanFlagValue(189);
            restartDreamOnUnocclude = reader.getBooleanFlagValue(190);
            restoreShowTapsSetting = reader.getBooleanFlagValue(191);
            restrictCommunalAppWidgetHostListening = reader.getBooleanFlagValue(192);
            restrictCommunalShadeToWhenIdle = reader.getBooleanFlagValue(193);
            revampedBouncerMessages = reader.getBooleanFlagValue(194);
            runFingerprintDetectOnDismissibleKeyguard = reader.getBooleanFlagValue(195);
            sceneContainer = reader.getBooleanFlagValue(196);
            screenOffAnimationGuardEnabled = reader.getBooleanFlagValue(197);
            screenReactions = reader.getBooleanFlagValue(198);
            screenshareNotificationHidingBugFix = reader.getBooleanFlagValue(199);
            screenshotAnnounceLiveRegion = reader.getBooleanFlagValue(200);
            screenshotDismissalSpring = reader.getBooleanFlagValue(201);
            screenshotForceShutterSound = reader.getBooleanFlagValue(202);
            screenshotMultidisplayFocusChange = reader.getBooleanFlagValue(203);
            screenshotPolicySplitAndDesktopMode = reader.getBooleanFlagValue(204);
            screenshotScrollCropViewCrashFix = reader.getBooleanFlagValue(205);
            scrimFix = reader.getBooleanFlagValue(206);
            secondaryUserWidgetHost = reader.getBooleanFlagValue(207);
            settingsExtRegisterContentObserverOnBgThread = reader.getBooleanFlagValue(208);
            shadeAppLaunchAnimationSkipInDesktop = reader.getBooleanFlagValue(209);
            shadeExpandsOnStatusBarLongPress = reader.getBooleanFlagValue(210);
            shadeQsvisibleLogic = reader.getBooleanFlagValue(213);
            shadeWindowGoesAround = reader.getBooleanFlagValue(214);
            shaderlibLoadingEffectRefactor = reader.getBooleanFlagValue(215);
            shortcutHelperKeyGlyph = reader.getBooleanFlagValue(216);
            shortcutHelperMultiDisplaySupport = reader.getBooleanFlagValue(217);
            showClipboardIndication = reader.getBooleanFlagValue(219);
            showIconInEmptyShade = reader.getBooleanFlagValue(220);
            showLockedByYourWatchKeyguardIndicator = reader.getBooleanFlagValue(221);
            simPinBouncerReset = reader.getBooleanFlagValue(223);
            sliceManagerBinderCallBackground = reader.getBooleanFlagValue(224);
            smartspaceRelocateToBottom = reader.getBooleanFlagValue(225);
            smartspaceSwipeEventLoggingFix = reader.getBooleanFlagValue(226);
            smartspaceViewpager2 = reader.getBooleanFlagValue(227);
            sounddoseCustomization = reader.getBooleanFlagValue(228);
            spatialModelAppPushback = reader.getBooleanFlagValue(229);
            spatialModelBouncerPushback = reader.getBooleanFlagValue(230);
            spatialModelPushbackInShader = reader.getBooleanFlagValue(231);
            stabilizeHeadsUpGroupV2 = reader.getBooleanFlagValue(232);
            statusBarAlwaysCheckUnderlyingNetworks = reader.getBooleanFlagValue(233);
            statusBarAlwaysScheduleAutoHide = reader.getBooleanFlagValue(234);
            statusBarAlwaysUseRegionSampling = reader.getBooleanFlagValue(235);
            statusBarAppHandleTracking = reader.getBooleanFlagValue(236);
            statusBarBatteryNoConflation = reader.getBooleanFlagValue(237);
            statusBarCallChipUseIsHidden = reader.getBooleanFlagValue(238);
            statusBarChipToHunAnimation = reader.getBooleanFlagValue(239);
            statusBarChipsModernization = reader.getBooleanFlagValue(240);
            statusBarChipsReturnAnimations = reader.getBooleanFlagValue(241);
            statusBarDarkIconInteractorMixedFix = reader.getBooleanFlagValue(242);
            statusBarDate = reader.getBooleanFlagValue(243);
            statusBarForDesktop = reader.getBooleanFlagValue(245);
            statusBarMobileIconKairos = reader.getBooleanFlagValue(246);
            statusBarNoHunBehavior = reader.getBooleanFlagValue(247);
            statusBarPopupChips = reader.getBooleanFlagValue(248);
            statusBarPrivacyChipAnimationExemption = reader.getBooleanFlagValue(249);
            statusBarRootModernization = reader.getBooleanFlagValue(251);
            statusBarRudimentaryBattery = reader.getBooleanFlagValue(252);
            statusBarShareDialogWithAppName = reader.getBooleanFlagValue(253);
            statusBarShowIconsInSecureCamera = reader.getBooleanFlagValue(254);
            statusBarStaticInoutIndicators = reader.getBooleanFlagValue(255);
            statusBarSwitchToSpnFromDataSpn = reader.getBooleanFlagValue(256);
            statusBarSystemStatusIconsInCompose = reader.getBooleanFlagValue(257);
            statusBarUiThread = reader.getBooleanFlagValue(258);
            statusBarUniversalBatteryDataSource = reader.getBooleanFlagValue(259);
            stuckHearingDevicesQsTileFix = reader.getBooleanFlagValue(260);
            switchUserOnBg = reader.getBooleanFlagValue(261);
            sysuiIntrinsicLockDispatcher = reader.getBooleanFlagValue(262);
            sysuiTeamfood = reader.getBooleanFlagValue(263);
            themeOverlayControllerWakefulnessDeprecation = reader.getBooleanFlagValue(264);
            thinScreenRecordingService = reader.getBooleanFlagValue(265);
            unfoldAnimationBackgroundProgress = reader.getBooleanFlagValue(266);
            updateKeyguardOnWakeAndUnlockEarlier = reader.getBooleanFlagValue(267);
            updateUserSwitcherBackground = reader.getBooleanFlagValue(268);
            userEncryptedSource = reader.getBooleanFlagValue(271);
            visualInterruptionsRefactor = reader.getBooleanFlagValue(273);
            volumeRedesign = reader.getBooleanFlagValue(274);
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

    public boolean activityTransitionUseLargestWindow() {
        if (!isCached) {
            init();
        }
        return activityTransitionUseLargestWindow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowDozeTouchesForLockIcon() {
        if (!isCached) {
            init();
        }
        return allowDozeTouchesForLockIcon;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ambientCuePlugin() {
        if (!isCached) {
            init();
        }
        return ambientCuePlugin;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ambientTouchMonitorListenToDisplayChanges() {
        if (!isCached) {
            init();
        }
        return ambientTouchMonitorListenToDisplayChanges;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean animationLibraryShellMigration() {
        if (!isCached) {
            init();
        }
        return animationLibraryShellMigration;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean appClipsBacklinks() {
        if (!isCached) {
            init();
        }
        return appClipsBacklinks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean avalancheReplaceHunWhenCritical() {
        if (!isCached) {
            init();
        }
        return avalancheReplaceHunWhenCritical;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean backButtonOnBouncer() {
        if (!isCached) {
            init();
        }
        return backButtonOnBouncer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean blockMouseEdgeBackGesture() {
        if (!isCached) {
            init();
        }
        return blockMouseEdgeBackGesture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean blurOnMoreSurfaces() {
        if (!isCached) {
            init();
        }
        return blurOnMoreSurfaces;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean blurSettingsToggle() {
        if (!isCached) {
            init();
        }
        return blurSettingsToggle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bouncerUiRevamp() {
        if (!isCached) {
            init();
        }
        return bouncerUiRevamp;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bouncerUiRevamp2() {
        if (!isCached) {
            init();
        }
        return bouncerUiRevamp2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bpColors() {
        if (!isCached) {
            init();
        }
        return bpColors;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean brightnessSliderFocusState() {
        if (!isCached) {
            init();
        }
        return brightnessSliderFocusState;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean captionsToggleInVolumeDialogV1() {
        if (!isCached) {
            init();
        }
        return captionsToggleInVolumeDialogV1;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean checkDesktopModeForSpacialModelAppPushback() {
        if (!isCached) {
            init();
        }
        return checkDesktopModeForSpacialModelAppPushback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean classicFlagsMultiUser() {
        if (!isCached) {
            init();
        }
        return classicFlagsMultiUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cleanupInstancesWhenDisplayRemoved() {
        if (!isCached) {
            init();
        }
        return cleanupInstancesWhenDisplayRemoved;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clearShortcutIconTint() {
        if (!isCached) {
            init();
        }
        return clearShortcutIconTint;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clipboardOverlayMultiuser() {
        if (!isCached) {
            init();
        }
        return clipboardOverlayMultiuser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clipboardUseDescriptionMimetype() {
        if (!isCached) {
            init();
        }
        return clipboardUseDescriptionMimetype;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clockFidgetAnimation() {
        if (!isCached) {
            init();
        }
        return clockFidgetAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean clockModernization() {
        if (!isCached) {
            init();
        }
        return clockModernization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalBouncerDoNotModifyPluginOpen() {
        if (!isCached) {
            init();
        }
        return communalBouncerDoNotModifyPluginOpen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalEditWidgetsActivityFinishFix() {
        if (!isCached) {
            init();
        }
        return communalEditWidgetsActivityFinishFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalHub() {
        if (!isCached) {
            init();
        }
        return communalHub;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalHubUseThreadPoolForWidgets() {
        if (!isCached) {
            init();
        }
        return communalHubUseThreadPoolForWidgets;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalPowerTransitionFix() {
        if (!isCached) {
            init();
        }
        return communalPowerTransitionFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalResponsiveGrid() {
        if (!isCached) {
            init();
        }
        return communalResponsiveGrid;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalShadeTouchHandlingFixes() {
        if (!isCached) {
            init();
        }
        return communalShadeTouchHandlingFixes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalStandaloneSupport() {
        if (!isCached) {
            init();
        }
        return communalStandaloneSupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalTimerFlickerFix() {
        if (!isCached) {
            init();
        }
        return communalTimerFlickerFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalWidgetResizing() {
        if (!isCached) {
            init();
        }
        return communalWidgetResizing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean communalWidgetTrampolineFix() {
        if (!isCached) {
            init();
        }
        return communalWidgetTrampolineFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean composeBouncer() {
        if (!isCached) {
            init();
        }
        return composeBouncer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean confineNotificationTouchToViewWidth() {
        if (!isCached) {
            init();
        }
        return confineNotificationTouchToViewWidth;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean contAuthPlugin() {
        if (!isCached) {
            init();
        }
        return contAuthPlugin;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean coroutineTracing() {
        if (!isCached) {
            init();
        }
        return coroutineTracing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean decoupleViewControllerInAnimlib() {
        if (!isCached) {
            init();
        }
        return decoupleViewControllerInAnimlib;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deferDozeTransitionOnShadeDrag() {
        if (!isCached) {
            init();
        }
        return deferDozeTransitionOnShadeDrag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean desktopAvControlsPopup() {
        if (!isCached) {
            init();
        }
        return desktopAvControlsPopup;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean desktopEffectsQsTile() {
        if (!isCached) {
            init();
        }
        return desktopEffectsQsTile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean desktopSizing() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dialogAnimEndStateUpdate() {
        if (!isCached) {
            init();
        }
        return dialogAnimEndStateUpdate;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableContextualTipsFrequencyCheck() {
        if (!isCached) {
            init();
        }
        return disableContextualTipsFrequencyCheck;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableContextualTipsIosSwitcherCheck() {
        if (!isCached) {
            init();
        }
        return disableContextualTipsIosSwitcherCheck;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableDoubleClickSwapOnBouncer() {
        if (!isCached) {
            init();
        }
        return disableDoubleClickSwapOnBouncer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableUserSwitcherDropdownOnBouncer() {
        if (!isCached) {
            init();
        }
        return disableUserSwitcherDropdownOnBouncer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean doNotUseImmediateCoroutineDispatcher() {
        if (!isCached) {
            init();
        }
        return doNotUseImmediateCoroutineDispatcher;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean doNotUseRunBlocking() {
        if (!isCached) {
            init();
        }
        return doNotUseRunBlocking;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean doubleTapToSleep() {
        if (!isCached) {
            init();
        }
        return doubleTapToSleep;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamBiometricPromptFixes() {
        if (!isCached) {
            init();
        }
        return dreamBiometricPromptFixes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamBouncerTransitionFix() {
        if (!isCached) {
            init();
        }
        return dreamBouncerTransitionFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamInputSessionPilferOnce() {
        if (!isCached) {
            init();
        }
        return dreamInputSessionPilferOnce;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamOverlayBouncerSwipeDirectionFiltering() {
        if (!isCached) {
            init();
        }
        return dreamOverlayBouncerSwipeDirectionFiltering;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamOverlayUpdatedUi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean dreamSuppression() {
        if (!isCached) {
            init();
        }
        return dreamSuppression;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean edgeBackGestureHandlerThread() {
        if (!isCached) {
            init();
        }
        return edgeBackGestureHandlerThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean edgebackGestureHandlerGetRunningTasksBackground() {
        if (!isCached) {
            init();
        }
        return edgebackGestureHandlerGetRunningTasksBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBackgroundKeyguardOndrawnCallback() {
        if (!isCached) {
            init();
        }
        return enableBackgroundKeyguardOndrawnCallback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableConstraintLayoutLockscreenOnExternalDisplay() {
        if (!isCached) {
            init();
        }
        return enableConstraintLayoutLockscreenOnExternalDisplay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableContextualTipForMuteVolume() {
        if (!isCached) {
            init();
        }
        return enableContextualTipForMuteVolume;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableCueBarAnimatedIcon() {
        if (!isCached) {
            init();
        }
        return enableCueBarAnimatedIcon;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDesktopGrowth() {
        if (!isCached) {
            init();
        }
        return enableDesktopGrowth;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableEfficientDisplayRepository() {
        if (!isCached) {
            init();
        }
        return enableEfficientDisplayRepository;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLayoutTracing() {
        if (!isCached) {
            init();
        }
        return enableLayoutTracing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMinmode() {
        if (!isCached) {
            init();
        }
        return enableMinmode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableOutputSwitcherAudioSharingButton() {
        if (!isCached) {
            init();
        }
        return enableOutputSwitcherAudioSharingButton;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSuggestedDeviceUi() {
        if (!isCached) {
            init();
        }
        return enableSuggestedDeviceUi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableUnderlay() {
        if (!isCached) {
            init();
        }
        return enableUnderlay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableViewCaptureTracing() {
        if (!isCached) {
            init();
        }
        return enableViewCaptureTracing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean exampleFlag() {
        if (!isCached) {
            init();
        }
        return exampleFlag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean expandCollapsePrivacyDialog() {
        if (!isCached) {
            init();
        }
        return expandCollapsePrivacyDialog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean expandHeadsUpOnInlineReply() {
        if (!isCached) {
            init();
        }
        return expandHeadsUpOnInlineReply;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean expandableUseModifierImplementation() {
        if (!isCached) {
            init();
        }
        return expandableUseModifierImplementation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean expandedPrivacyIndicatorsOnLargeScreen() {
        if (!isCached) {
            init();
        }
        return expandedPrivacyIndicatorsOnLargeScreen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean extendedAppsShortcutCategory() {
        if (!isCached) {
            init();
        }
        return extendedAppsShortcutCategory;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean faceScanningAnimationNpeFix() {
        if (!isCached) {
            init();
        }
        return faceScanningAnimationNpeFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fetchBookmarksXmlKeyboardShortcuts() {
        if (!isCached) {
            init();
        }
        return fetchBookmarksXmlKeyboardShortcuts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixShadeHeaderWrongIconSize() {
        if (!isCached) {
            init();
        }
        return fixShadeHeaderWrongIconSize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean flashlightStrength() {
        if (!isCached) {
            init();
        }
        return flashlightStrength;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean floatingMenuDragToHide() {
        if (!isCached) {
            init();
        }
        return floatingMenuDragToHide;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean floatingMenuHearingDeviceStatusIcon() {
        if (!isCached) {
            init();
        }
        return floatingMenuHearingDeviceStatusIcon;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean floatingMenuRadiiAnimation() {
        if (!isCached) {
            init();
        }
        return floatingMenuRadiiAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean gestureBetweenHubAndLockscreenMotion() {
        if (!isCached) {
            init();
        }
        return gestureBetweenHubAndLockscreenMotion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean getConnectedDeviceNameUnsynchronized() {
        if (!isCached) {
            init();
        }
        return getConnectedDeviceNameUnsynchronized;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubAllowKeyguardWhenDreaming() {
        if (!isCached) {
            init();
        }
        return glanceableHubAllowKeyguardWhenDreaming;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubBlurredBackground() {
        if (!isCached) {
            init();
        }
        return glanceableHubBlurredBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubDirectEditMode() {
        if (!isCached) {
            init();
        }
        return glanceableHubDirectEditMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubEnabledByDefault() {
        if (!isCached) {
            init();
        }
        return glanceableHubEnabledByDefault;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubV2() {
        if (!isCached) {
            init();
        }
        return glanceableHubV2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean glanceableHubV2Resources() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean globalActionsEmphasizedFont() {
        if (!isCached) {
            init();
        }
        return globalActionsEmphasizedFont;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hardwareColorStyles() {
        if (!isCached) {
            init();
        }
        return hardwareColorStyles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hearingAidsQsTileDialog() {
        if (!isCached) {
            init();
        }
        return hearingAidsQsTileDialog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hearingDevicesDialogRelatedTools() {
        if (!isCached) {
            init();
        }
        return hearingDevicesDialogRelatedTools;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hideRingerButtonInSingleVolumeMode() {
        if (!isCached) {
            init();
        }
        return hideRingerButtonInSingleVolumeMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean homeControlsDreamHsum() {
        if (!isCached) {
            init();
        }
        return homeControlsDreamHsum;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hsuQsChanges() {
        if (!isCached) {
            init();
        }
        return hsuQsChanges;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hubBlurredByShadeFix() {
        if (!isCached) {
            init();
        }
        return hubBlurredByShadeFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hubEditModeTouchAdjustments() {
        if (!isCached) {
            init();
        }
        return hubEditModeTouchAdjustments;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hubEditModeTransition() {
        if (!isCached) {
            init();
        }
        return hubEditModeTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean iconRefresh2025() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean indicationTextA11yFix() {
        if (!isCached) {
            init();
        }
        return indicationTextA11yFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyboardDockingIndicator() {
        if (!isCached) {
            init();
        }
        return keyboardDockingIndicator;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyboardShortcutHelperRewrite() {
        if (!isCached) {
            init();
        }
        return keyboardShortcutHelperRewrite;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyboardTouchpadContextualEducation() {
        if (!isCached) {
            init();
        }
        return keyboardTouchpadContextualEducation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyguardTransitionForceFinishOnScreenOff() {
        if (!isCached) {
            init();
        }
        return keyguardTransitionForceFinishOnScreenOff;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean keyguardWmStateRefactor() {
        if (!isCached) {
            init();
        }
        return keyguardWmStateRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean largeScreenRecording() {
        if (!isCached) {
            init();
        }
        return largeScreenRecording;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean largeScreenScreencapture() {
        if (!isCached) {
            init();
        }
        return largeScreenScreencapture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean largeScreenScreenshotAppWindow() {
        if (!isCached) {
            init();
        }
        return largeScreenScreenshotAppWindow;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean largeScreenSharing() {
        if (!isCached) {
            init();
        }
        return largeScreenSharing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lockscreenShadeToDreamTransitionFix() {
        if (!isCached) {
            init();
        }
        return lockscreenShadeToDreamTransitionFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lowLightClockDream() {
        if (!isCached) {
            init();
        }
        return lowLightClockDream;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lowlightClockSetBrightness() {
        if (!isCached) {
            init();
        }
        return lowlightClockSetBrightness;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lowlightClockUsesKeyguardChargingStatus() {
        if (!isCached) {
            init();
        }
        return lowlightClockUsesKeyguardChargingStatus;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean magneticNotificationSwipes() {
        if (!isCached) {
            init();
        }
        return magneticNotificationSwipes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaCarouselArrows() {
        if (!isCached) {
            init();
        }
        return mediaCarouselArrows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaControlsButtonMedia3() {
        if (!isCached) {
            init();
        }
        return mediaControlsButtonMedia3;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaControlsButtonMedia3Placement() {
        if (!isCached) {
            init();
        }
        return mediaControlsButtonMedia3Placement;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaControlsInCompose() {
        if (!isCached) {
            init();
        }
        return mediaControlsInCompose;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaControlsTranslationFix() {
        if (!isCached) {
            init();
        }
        return mediaControlsTranslationFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaFrameDimensionsFix() {
        if (!isCached) {
            init();
        }
        return mediaFrameDimensionsFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaProjectionDialogBehindLockscreen() {
        if (!isCached) {
            init();
        }
        return mediaProjectionDialogBehindLockscreen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mediaProjectionGreyErrorText() {
        if (!isCached) {
            init();
        }
        return mediaProjectionGreyErrorText;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean modesUiDialogPaging() {
        if (!isCached) {
            init();
        }
        return modesUiDialogPaging;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean moveTransitionAnimationLayer() {
        if (!isCached) {
            init();
        }
        return moveTransitionAnimationLayer;
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

    public boolean multiuserOpenUserSwitcherDialog() {
        if (!isCached) {
            init();
        }
        return multiuserOpenUserSwitcherDialog;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean multiuserWifiPickerTrackerSupport() {
        if (!isCached) {
            init();
        }
        return multiuserWifiPickerTrackerSupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newAodTransition() {
        if (!isCached) {
            init();
        }
        return newAodTransition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newDozingKeyguardStates() {
        if (!isCached) {
            init();
        }
        return newDozingKeyguardStates;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newScreenRecordToolbar() {
        if (!isCached) {
            init();
        }
        return newScreenRecordToolbar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newVolumePanel() {
        if (!isCached) {
            init();
        }
        return newVolumePanel;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean noExpansionOnOverscroll() {
        if (!isCached) {
            init();
        }
        return noExpansionOnOverscroll;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean noShadeBlurOnDreamStart() {
        if (!isCached) {
            init();
        }
        return noShadeBlurOnDreamStart;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean nonTouchscreenDevicesBypassFalsing() {
        if (!isCached) {
            init();
        }
        return nonTouchscreenDevicesBypassFalsing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notesRoleQsTile() {
        if (!isCached) {
            init();
        }
        return notesRoleQsTile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAddXOnHoverToDismiss() {
        if (!isCached) {
            init();
        }
        return notificationAddXOnHoverToDismiss;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAmbientSuppressionAfterInflation() {
        if (!isCached) {
            init();
        }
        return notificationAmbientSuppressionAfterInflation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAnimatedActionsTreatment() {
        if (!isCached) {
            init();
        }
        return notificationAnimatedActionsTreatment;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAppearNonlinear() {
        if (!isCached) {
            init();
        }
        return notificationAppearNonlinear;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAsyncGroupHeaderInflation() {
        if (!isCached) {
            init();
        }
        return notificationAsyncGroupHeaderInflation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAvalancheSuppression() {
        if (!isCached) {
            init();
        }
        return notificationAvalancheSuppression;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationAvalancheThrottleHun() {
        if (!isCached) {
            init();
        }
        return notificationAvalancheThrottleHun;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationBackgroundTintOptimization() {
        if (!isCached) {
            init();
        }
        return notificationBackgroundTintOptimization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationBundleUi() {
        if (!isCached) {
            init();
        }
        return notificationBundleUi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationChildrenContainerMinHeight() {
        if (!isCached) {
            init();
        }
        return notificationChildrenContainerMinHeight;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationColorUpdateLogger() {
        if (!isCached) {
            init();
        }
        return notificationColorUpdateLogger;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationFixHunShadows() {
        if (!isCached) {
            init();
        }
        return notificationFixHunShadows;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationFooterBackgroundTintOptimization() {
        if (!isCached) {
            init();
        }
        return notificationFooterBackgroundTintOptimization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationRowIsRemovedFix() {
        if (!isCached) {
            init();
        }
        return notificationRowIsRemovedFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationRowTransparency() {
        if (!isCached) {
            init();
        }
        return notificationRowTransparency;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationShadeBlur() {
        if (!isCached) {
            init();
        }
        return notificationShadeBlur;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationShadeUiThread() {
        if (!isCached) {
            init();
        }
        return notificationShadeUiThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationSkipSilentUpdates() {
        if (!isCached) {
            init();
        }
        return notificationSkipSilentUpdates;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationTransparentHeaderFix() {
        if (!isCached) {
            init();
        }
        return notificationTransparentHeaderFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationsHideOnDisplaySwitch() {
        if (!isCached) {
            init();
        }
        return notificationsHideOnDisplaySwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationsIconContainerRefactor() {
        if (!isCached) {
            init();
        }
        return notificationsIconContainerRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationsRedesignFooterView() {
        if (!isCached) {
            init();
        }
        return notificationsRedesignFooterView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ongoingActivityChipsOnDream() {
        if (!isCached) {
            init();
        }
        return ongoingActivityChipsOnDream;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean overrideSuppressOverlayCondition() {
        if (!isCached) {
            init();
        }
        return overrideSuppressOverlayCondition;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean permissionHelperInlineUiRichOngoing() {
        if (!isCached) {
            init();
        }
        return permissionHelperInlineUiRichOngoing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean permissionHelperUiRichOngoing() {
        if (!isCached) {
            init();
        }
        return permissionHelperUiRichOngoing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean physicalNotificationMovement() {
        if (!isCached) {
            init();
        }
        return physicalNotificationMovement;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean pinInputFieldStyledFocusState() {
        if (!isCached) {
            init();
        }
        return pinInputFieldStyledFocusState;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean predictiveBackAnimateShade() {
        if (!isCached) {
            init();
        }
        return predictiveBackAnimateShade;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privacyDotLiveRegion() {
        if (!isCached) {
            init();
        }
        return privacyDotLiveRegion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean promoteNotificationsAutomatically() {
        if (!isCached) {
            init();
        }
        return promoteNotificationsAutomatically;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean pssTaskSwitcher() {
        if (!isCached) {
            init();
        }
        return pssTaskSwitcher;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsComposeFragmentEarlyExpansion() {
        if (!isCached) {
            init();
        }
        return qsComposeFragmentEarlyExpansion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsEditModeTooltip() {
        if (!isCached) {
            init();
        }
        return qsEditModeTooltip;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsEditModeV2() {
        if (!isCached) {
            init();
        }
        return qsEditModeV2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsMaterialExpressiveTiles() {
        if (!isCached) {
            init();
        }
        return qsMaterialExpressiveTiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsNewTiles() {
        if (!isCached) {
            init();
        }
        return qsNewTiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsNewTilesFuture() {
        if (!isCached) {
            init();
        }
        return qsNewTilesFuture;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsSplitInternetTile() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsTileDetailedView() {
        if (!isCached) {
            init();
        }
        return qsTileDetailedView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsTileFocusState() {
        if (!isCached) {
            init();
        }
        return qsTileFocusState;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsTileTransitionInteractionRefinement() {
        if (!isCached) {
            init();
        }
        return qsTileTransitionInteractionRefinement;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsUiRefactorComposeFragment() {
        if (!isCached) {
            init();
        }
        return qsUiRefactorComposeFragment;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean qsWifiConfig() {
        if (!isCached) {
            init();
        }
        return qsWifiConfig;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean recordIssueQsTile() {
        if (!isCached) {
            init();
        }
        return recordIssueQsTile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean redesignMagnificationWindowSize() {
        if (!isCached) {
            init();
        }
        return redesignMagnificationWindowSize;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean registerWallpaperNotifierBackground() {
        if (!isCached) {
            init();
        }
        return registerWallpaperNotifierBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeDreamOverlayHideOnTouch() {
        if (!isCached) {
            init();
        }
        return removeDreamOverlayHideOnTouch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeNearbyShareTileAnimation() {
        if (!isCached) {
            init();
        }
        return removeNearbyShareTileAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeUpdateListenerInQsIconViewImpl() {
        if (!isCached) {
            init();
        }
        return removeUpdateListenerInQsIconViewImpl;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean resetTilesRemovesCustomTiles() {
        if (!isCached) {
            init();
        }
        return resetTilesRemovesCustomTiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restToUnlock() {
        if (!isCached) {
            init();
        }
        return restToUnlock;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restartDreamOnUnocclude() {
        if (!isCached) {
            init();
        }
        return restartDreamOnUnocclude;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restoreShowTapsSetting() {
        if (!isCached) {
            init();
        }
        return restoreShowTapsSetting;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restrictCommunalAppWidgetHostListening() {
        if (!isCached) {
            init();
        }
        return restrictCommunalAppWidgetHostListening;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restrictCommunalShadeToWhenIdle() {
        if (!isCached) {
            init();
        }
        return restrictCommunalShadeToWhenIdle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean revampedBouncerMessages() {
        if (!isCached) {
            init();
        }
        return revampedBouncerMessages;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean runFingerprintDetectOnDismissibleKeyguard() {
        if (!isCached) {
            init();
        }
        return runFingerprintDetectOnDismissibleKeyguard;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sceneContainer() {
        if (!isCached) {
            init();
        }
        return sceneContainer;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenOffAnimationGuardEnabled() {
        if (!isCached) {
            init();
        }
        return screenOffAnimationGuardEnabled;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenReactions() {
        if (!isCached) {
            init();
        }
        return screenReactions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshareNotificationHidingBugFix() {
        if (!isCached) {
            init();
        }
        return screenshareNotificationHidingBugFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotAnnounceLiveRegion() {
        if (!isCached) {
            init();
        }
        return screenshotAnnounceLiveRegion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotDismissalSpring() {
        if (!isCached) {
            init();
        }
        return screenshotDismissalSpring;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotForceShutterSound() {
        if (!isCached) {
            init();
        }
        return screenshotForceShutterSound;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotMultidisplayFocusChange() {
        if (!isCached) {
            init();
        }
        return screenshotMultidisplayFocusChange;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotPolicySplitAndDesktopMode() {
        if (!isCached) {
            init();
        }
        return screenshotPolicySplitAndDesktopMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean screenshotScrollCropViewCrashFix() {
        if (!isCached) {
            init();
        }
        return screenshotScrollCropViewCrashFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scrimFix() {
        if (!isCached) {
            init();
        }
        return scrimFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean secondaryUserWidgetHost() {
        if (!isCached) {
            init();
        }
        return secondaryUserWidgetHost;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean settingsExtRegisterContentObserverOnBgThread() {
        if (!isCached) {
            init();
        }
        return settingsExtRegisterContentObserverOnBgThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeAppLaunchAnimationSkipInDesktop() {
        if (!isCached) {
            init();
        }
        return shadeAppLaunchAnimationSkipInDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeExpandsOnStatusBarLongPress() {
        if (!isCached) {
            init();
        }
        return shadeExpandsOnStatusBarLongPress;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeHeaderBlurFontColor() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeHeaderFontUpdate() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeQsvisibleLogic() {
        if (!isCached) {
            init();
        }
        return shadeQsvisibleLogic;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shadeWindowGoesAround() {
        if (!isCached) {
            init();
        }
        return shadeWindowGoesAround;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shaderlibLoadingEffectRefactor() {
        if (!isCached) {
            init();
        }
        return shaderlibLoadingEffectRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shortcutHelperKeyGlyph() {
        if (!isCached) {
            init();
        }
        return shortcutHelperKeyGlyph;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean shortcutHelperMultiDisplaySupport() {
        if (!isCached) {
            init();
        }
        return shortcutHelperMultiDisplaySupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showAudioSharingSliderInVolumePanel() {
        if (!isCached) {
            init();
        }
        return showAudioSharingSliderInVolumePanel;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showClipboardIndication() {
        if (!isCached) {
            init();
        }
        return showClipboardIndication;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showIconInEmptyShade() {
        if (!isCached) {
            init();
        }
        return showIconInEmptyShade;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean showLockedByYourWatchKeyguardIndicator() {
        if (!isCached) {
            init();
        }
        return showLockedByYourWatchKeyguardIndicator;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean signOutButtonOnKeyguardStatusBar() {
        if (!isCached) {
            init();
        }
        return signOutButtonOnKeyguardStatusBar;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean simPinBouncerReset() {
        if (!isCached) {
            init();
        }
        return simPinBouncerReset;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sliceManagerBinderCallBackground() {
        if (!isCached) {
            init();
        }
        return sliceManagerBinderCallBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceRelocateToBottom() {
        if (!isCached) {
            init();
        }
        return smartspaceRelocateToBottom;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceSwipeEventLoggingFix() {
        if (!isCached) {
            init();
        }
        return smartspaceSwipeEventLoggingFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean smartspaceViewpager2() {
        if (!isCached) {
            init();
        }
        return smartspaceViewpager2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sounddoseCustomization() {
        if (!isCached) {
            init();
        }
        return sounddoseCustomization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean spatialModelAppPushback() {
        if (!isCached) {
            init();
        }
        return spatialModelAppPushback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean spatialModelBouncerPushback() {
        if (!isCached) {
            init();
        }
        return spatialModelBouncerPushback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean spatialModelPushbackInShader() {
        if (!isCached) {
            init();
        }
        return spatialModelPushbackInShader;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean stabilizeHeadsUpGroupV2() {
        if (!isCached) {
            init();
        }
        return stabilizeHeadsUpGroupV2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarAlwaysCheckUnderlyingNetworks() {
        if (!isCached) {
            init();
        }
        return statusBarAlwaysCheckUnderlyingNetworks;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarAlwaysScheduleAutoHide() {
        if (!isCached) {
            init();
        }
        return statusBarAlwaysScheduleAutoHide;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarAlwaysUseRegionSampling() {
        if (!isCached) {
            init();
        }
        return statusBarAlwaysUseRegionSampling;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarAppHandleTracking() {
        if (!isCached) {
            init();
        }
        return statusBarAppHandleTracking;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarBatteryNoConflation() {
        if (!isCached) {
            init();
        }
        return statusBarBatteryNoConflation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarCallChipUseIsHidden() {
        if (!isCached) {
            init();
        }
        return statusBarCallChipUseIsHidden;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarChipToHunAnimation() {
        if (!isCached) {
            init();
        }
        return statusBarChipToHunAnimation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarChipsModernization() {
        if (!isCached) {
            init();
        }
        return statusBarChipsModernization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarChipsReturnAnimations() {
        if (!isCached) {
            init();
        }
        return statusBarChipsReturnAnimations;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarDarkIconInteractorMixedFix() {
        if (!isCached) {
            init();
        }
        return statusBarDarkIconInteractorMixedFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarDate() {
        if (!isCached) {
            init();
        }
        return statusBarDate;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarFontUpdates() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarForDesktop() {
        if (!isCached) {
            init();
        }
        return statusBarForDesktop;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarMobileIconKairos() {
        if (!isCached) {
            init();
        }
        return statusBarMobileIconKairos;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarNoHunBehavior() {
        if (!isCached) {
            init();
        }
        return statusBarNoHunBehavior;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarPopupChips() {
        if (!isCached) {
            init();
        }
        return statusBarPopupChips;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarPrivacyChipAnimationExemption() {
        if (!isCached) {
            init();
        }
        return statusBarPrivacyChipAnimationExemption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarRegionSampling() {
        if (!isCached) {
            init();
        }
        return statusBarRegionSampling;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarRootModernization() {
        if (!isCached) {
            init();
        }
        return statusBarRootModernization;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarRudimentaryBattery() {
        if (!isCached) {
            init();
        }
        return statusBarRudimentaryBattery;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarShareDialogWithAppName() {
        if (!isCached) {
            init();
        }
        return statusBarShareDialogWithAppName;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarShowIconsInSecureCamera() {
        if (!isCached) {
            init();
        }
        return statusBarShowIconsInSecureCamera;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarStaticInoutIndicators() {
        if (!isCached) {
            init();
        }
        return statusBarStaticInoutIndicators;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarSwitchToSpnFromDataSpn() {
        if (!isCached) {
            init();
        }
        return statusBarSwitchToSpnFromDataSpn;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarSystemStatusIconsInCompose() {
        if (!isCached) {
            init();
        }
        return statusBarSystemStatusIconsInCompose;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarUiThread() {
        if (!isCached) {
            init();
        }
        return statusBarUiThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarUniversalBatteryDataSource() {
        if (!isCached) {
            init();
        }
        return statusBarUniversalBatteryDataSource;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean stuckHearingDevicesQsTileFix() {
        if (!isCached) {
            init();
        }
        return stuckHearingDevicesQsTileFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean switchUserOnBg() {
        if (!isCached) {
            init();
        }
        return switchUserOnBg;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sysuiIntrinsicLockDispatcher() {
        if (!isCached) {
            init();
        }
        return sysuiIntrinsicLockDispatcher;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean sysuiTeamfood() {
        if (!isCached) {
            init();
        }
        return sysuiTeamfood;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean themeOverlayControllerWakefulnessDeprecation() {
        if (!isCached) {
            init();
        }
        return themeOverlayControllerWakefulnessDeprecation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean thinScreenRecordingService() {
        if (!isCached) {
            init();
        }
        return thinScreenRecordingService;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean unfoldAnimationBackgroundProgress() {
        if (!isCached) {
            init();
        }
        return unfoldAnimationBackgroundProgress;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateKeyguardOnWakeAndUnlockEarlier() {
        if (!isCached) {
            init();
        }
        return updateKeyguardOnWakeAndUnlockEarlier;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateUserSwitcherBackground() {
        if (!isCached) {
            init();
        }
        return updateUserSwitcherBackground;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateWindowMagnifierBottomBoundaryWithMouse() {
        if (!isCached) {
            init();
        }
        return updateWindowMagnifierBottomBoundaryWithMouse;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useAadProxSensorIfPresent() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean userEncryptedSource() {
        if (!isCached) {
            init();
        }
        return userEncryptedSource;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean userSwitcherAddSignOutOption() {
        if (!isCached) {
            init();
        }
        return userSwitcherAddSignOutOption;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean visualInterruptionsRefactor() {
        if (!isCached) {
            init();
        }
        return visualInterruptionsRefactor;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean volumeRedesign() {
        if (!isCached) {
            init();
        }
        return volumeRedesign;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean windowMagnificationMoveWithMouseOnEdge() {
        if (!isCached) {
            init();
        }
        return windowMagnificationMoveWithMouseOnEdge;
    }

}
