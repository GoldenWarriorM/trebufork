package android.multiuser;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean addLauncherUserConfig();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowMainUserToAccessBlockedNumberProvider();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowResolverSheetForPrivateSpace();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowSupervisingProfile();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean backupActivatedForAllUsers();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean blockPrivateSpaceCreation();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cacheProfileTypeReadOnly();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cacheUserRestrictionsReadOnly();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cacheUserStartRealtimeReadOnly();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cacheUserUnlockRealtimeReadOnly();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean consistentMaxUsers();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean createInitialUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean decoupleMaxUsersFromProfiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean demoteMainUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disablePrivateSpaceItemsOnHome();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disallowRemovingLastAdminUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableBiometricsToUnlockPrivateSpace();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableHidingProfiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMovingContentIntoPrivateSpace();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePermissionToAccessHiddenProfiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePrivateSpaceAutolockOnRestarts();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePrivateSpaceFeatures();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePrivateSpaceIntentRedirection();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enablePsSensitiveNotificationsToggle();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableSystemUserOnlyForServicesAndProviders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixAvatarContentProviderNullAuthority();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hsuAllowlistActivities();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hsuDeviceProvisioner();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean hsuNotAdmin();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean logoutUserApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean maxUsersInCarIsForSecondary();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean moveQuietModeOperationsToSeparateThread();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean moveSetScreenLockDialogToSettingsApp();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean multiuserWidget();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean perfettoMultiuserTable();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean privateSpaceFileLimitCheckTimeout();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean profilesForAll();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean propertyInvalidatedCacheBypassMismatchedUids();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean reorderWallpaperDuringUserSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean rescheduleStopIfVisibleActivities();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean restrictQuietModeCredentialBugFixToManagedProfiles();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean saveGlobalAndGuestRestrictionsOnSystemUserXml();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean saveGlobalAndGuestRestrictionsOnSystemUserXmlReadOnly();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scheduleStopOfBackgroundUser();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean scheduleStopOfBackgroundUserByDefault();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean setPowerModeDuringUserSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean setupwizardUsernamePopulation();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean stopExcessForBackgroundStarts();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean stopPreviousUserApps();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean supportCommunalProfile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean supportCommunalProfileNextgen();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean unicornModeRefactoringForHsumReadOnly();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useAllCpusDuringUserSwitch();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean usePrivateSpaceIconInBiometricPrompt();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean userFilterRefactoring();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean userRestrictionConfigWifiSharedPrivate();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean widgetCurrentUserView();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean widgetScalingBugfix();
}
