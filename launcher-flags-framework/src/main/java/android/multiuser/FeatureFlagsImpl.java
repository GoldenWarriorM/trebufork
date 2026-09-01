package android.multiuser;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean addLauncherUserConfig = true;
    private static boolean allowMainUserToAccessBlockedNumberProvider = true;
    private static boolean allowResolverSheetForPrivateSpace = true;
    private static boolean allowSupervisingProfile = true;
    private static boolean bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch = true;
    private static boolean consistentMaxUsers = true;
    private static boolean createInitialUser = true;
    private static boolean decoupleMaxUsersFromProfiles = true;
    private static boolean demoteMainUser = true;
    private static boolean disablePrivateSpaceItemsOnHome = true;
    private static boolean disallowRemovingLastAdminUser = true;
    private static boolean enableBiometricsToUnlockPrivateSpace = true;
    private static boolean enableHidingProfiles = true;
    private static boolean enableMovingContentIntoPrivateSpace = true;
    private static boolean enablePermissionToAccessHiddenProfiles = true;
    private static boolean enablePrivateSpaceAutolockOnRestarts = true;
    private static boolean enablePrivateSpaceFeatures = true;
    private static boolean enablePrivateSpaceIntentRedirection = true;
    private static boolean enablePsSensitiveNotificationsToggle = true;
    private static boolean fixAvatarContentProviderNullAuthority = false;
    private static boolean hsuAllowlistActivities = false;
    private static boolean hsuDeviceProvisioner = true;
    private static boolean hsuNotAdmin = false;
    private static boolean logoutUserApi = true;
    private static boolean maxUsersInCarIsForSecondary = true;
    private static boolean moveQuietModeOperationsToSeparateThread = true;
    private static boolean moveSetScreenLockDialogToSettingsApp = true;
    private static boolean multiuserWidget = false;
    private static boolean perfettoMultiuserTable = true;
    private static boolean privateSpaceFileLimitCheckTimeout = true;
    private static boolean profilesForAll = true;
    private static boolean reorderWallpaperDuringUserSwitch = true;
    private static boolean rescheduleStopIfVisibleActivities = true;
    private static boolean restrictQuietModeCredentialBugFixToManagedProfiles = true;
    private static boolean saveGlobalAndGuestRestrictionsOnSystemUserXml = true;
    private static boolean scheduleStopOfBackgroundUser = true;
    private static boolean scheduleStopOfBackgroundUserByDefault = true;
    private static boolean setPowerModeDuringUserSwitch = true;
    private static boolean setupwizardUsernamePopulation = false;
    private static boolean stopExcessForBackgroundStarts = true;
    private static boolean stopPreviousUserApps = true;
    private static boolean supportCommunalProfile = true;
    private static boolean supportCommunalProfileNextgen = false;
    private static boolean useAllCpusDuringUserSwitch = true;
    private static boolean usePrivateSpaceIconInBiometricPrompt = true;
    private static boolean userFilterRefactoring = false;
    private static boolean userRestrictionConfigWifiSharedPrivate = true;
    private static boolean widgetCurrentUserView = true;
    private static boolean widgetScalingBugfix = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.multiuser", 0xA76765B92D1A7607L);
            allowMainUserToAccessBlockedNumberProvider = reader.getBooleanFlagValue(1);
            bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch = reader.getBooleanFlagValue(5);
            consistentMaxUsers = reader.getBooleanFlagValue(7);
            createInitialUser = reader.getBooleanFlagValue(8);
            decoupleMaxUsersFromProfiles = reader.getBooleanFlagValue(9);
            demoteMainUser = reader.getBooleanFlagValue(10);
            disallowRemovingLastAdminUser = reader.getBooleanFlagValue(12);
            fixAvatarContentProviderNullAuthority = reader.getBooleanFlagValue(22);
            hsuAllowlistActivities = reader.getBooleanFlagValue(23);
            hsuDeviceProvisioner = reader.getBooleanFlagValue(24);
            hsuNotAdmin = reader.getBooleanFlagValue(25);
            logoutUserApi = reader.getBooleanFlagValue(26);
            maxUsersInCarIsForSecondary = reader.getBooleanFlagValue(27);
            multiuserWidget = reader.getBooleanFlagValue(30);
            perfettoMultiuserTable = reader.getBooleanFlagValue(31);
            profilesForAll = reader.getBooleanFlagValue(33);
            reorderWallpaperDuringUserSwitch = reader.getBooleanFlagValue(34);
            rescheduleStopIfVisibleActivities = reader.getBooleanFlagValue(35);
            saveGlobalAndGuestRestrictionsOnSystemUserXml = reader.getBooleanFlagValue(37);
            scheduleStopOfBackgroundUser = reader.getBooleanFlagValue(39);
            scheduleStopOfBackgroundUserByDefault = reader.getBooleanFlagValue(40);
            setPowerModeDuringUserSwitch = reader.getBooleanFlagValue(41);
            setupwizardUsernamePopulation = reader.getBooleanFlagValue(42);
            stopExcessForBackgroundStarts = reader.getBooleanFlagValue(43);
            stopPreviousUserApps = reader.getBooleanFlagValue(44);
            supportCommunalProfile = reader.getBooleanFlagValue(45);
            supportCommunalProfileNextgen = reader.getBooleanFlagValue(46);
            useAllCpusDuringUserSwitch = reader.getBooleanFlagValue(48);
            userFilterRefactoring = reader.getBooleanFlagValue(50);
            userRestrictionConfigWifiSharedPrivate = reader.getBooleanFlagValue(51);
            widgetCurrentUserView = reader.getBooleanFlagValue(52);
            widgetScalingBugfix = reader.getBooleanFlagValue(53);
            addLauncherUserConfig = reader.getBooleanFlagValue(0);
            allowResolverSheetForPrivateSpace = reader.getBooleanFlagValue(2);
            disablePrivateSpaceItemsOnHome = reader.getBooleanFlagValue(11);
            enableBiometricsToUnlockPrivateSpace = reader.getBooleanFlagValue(13);
            enableHidingProfiles = reader.getBooleanFlagValue(14);
            enableMovingContentIntoPrivateSpace = reader.getBooleanFlagValue(15);
            enablePermissionToAccessHiddenProfiles = reader.getBooleanFlagValue(16);
            enablePrivateSpaceAutolockOnRestarts = reader.getBooleanFlagValue(17);
            enablePrivateSpaceFeatures = reader.getBooleanFlagValue(18);
            enablePrivateSpaceIntentRedirection = reader.getBooleanFlagValue(19);
            enablePsSensitiveNotificationsToggle = reader.getBooleanFlagValue(20);
            moveQuietModeOperationsToSeparateThread = reader.getBooleanFlagValue(28);
            moveSetScreenLockDialogToSettingsApp = reader.getBooleanFlagValue(29);
            privateSpaceFileLimitCheckTimeout = reader.getBooleanFlagValue(32);
            restrictQuietModeCredentialBugFixToManagedProfiles = reader.getBooleanFlagValue(36);
            usePrivateSpaceIconInBiometricPrompt = reader.getBooleanFlagValue(49);
            allowSupervisingProfile = reader.getBooleanFlagValue(3);
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

    public boolean addLauncherUserConfig() {
        if (!isCached) {
            init();
        }
        return addLauncherUserConfig;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowMainUserToAccessBlockedNumberProvider() {
        if (!isCached) {
            init();
        }
        return allowMainUserToAccessBlockedNumberProvider;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowResolverSheetForPrivateSpace() {
        if (!isCached) {
            init();
        }
        return allowResolverSheetForPrivateSpace;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowSupervisingProfile() {
        if (!isCached) {
            init();
        }
        return allowSupervisingProfile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean backupActivatedForAllUsers() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch() {
        if (!isCached) {
            init();
        }
        return bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean blockPrivateSpaceCreation() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cacheProfileTypeReadOnly() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cacheUserRestrictionsReadOnly() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cacheUserStartRealtimeReadOnly() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cacheUserUnlockRealtimeReadOnly() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean consistentMaxUsers() {
        if (!isCached) {
            init();
        }
        return consistentMaxUsers;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean createInitialUser() {
        if (!isCached) {
            init();
        }
        return createInitialUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean decoupleMaxUsersFromProfiles() {
        if (!isCached) {
            init();
        }
        return decoupleMaxUsersFromProfiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean demoteMainUser() {
        if (!isCached) {
            init();
        }
        return demoteMainUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disablePrivateSpaceItemsOnHome() {
        if (!isCached) {
            init();
        }
        return disablePrivateSpaceItemsOnHome;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disallowRemovingLastAdminUser() {
        if (!isCached) {
            init();
        }
        return disallowRemovingLastAdminUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableBiometricsToUnlockPrivateSpace() {
        if (!isCached) {
            init();
        }
        return enableBiometricsToUnlockPrivateSpace;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHidingProfiles() {
        if (!isCached) {
            init();
        }
        return enableHidingProfiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMovingContentIntoPrivateSpace() {
        if (!isCached) {
            init();
        }
        return enableMovingContentIntoPrivateSpace;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePermissionToAccessHiddenProfiles() {
        if (!isCached) {
            init();
        }
        return enablePermissionToAccessHiddenProfiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePrivateSpaceAutolockOnRestarts() {
        if (!isCached) {
            init();
        }
        return enablePrivateSpaceAutolockOnRestarts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePrivateSpaceFeatures() {
        if (!isCached) {
            init();
        }
        return enablePrivateSpaceFeatures;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePrivateSpaceIntentRedirection() {
        if (!isCached) {
            init();
        }
        return enablePrivateSpaceIntentRedirection;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enablePsSensitiveNotificationsToggle() {
        if (!isCached) {
            init();
        }
        return enablePsSensitiveNotificationsToggle;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableSystemUserOnlyForServicesAndProviders() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixAvatarContentProviderNullAuthority() {
        if (!isCached) {
            init();
        }
        return fixAvatarContentProviderNullAuthority;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hsuAllowlistActivities() {
        if (!isCached) {
            init();
        }
        return hsuAllowlistActivities;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hsuDeviceProvisioner() {
        if (!isCached) {
            init();
        }
        return hsuDeviceProvisioner;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean hsuNotAdmin() {
        if (!isCached) {
            init();
        }
        return hsuNotAdmin;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean logoutUserApi() {
        if (!isCached) {
            init();
        }
        return logoutUserApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean maxUsersInCarIsForSecondary() {
        if (!isCached) {
            init();
        }
        return maxUsersInCarIsForSecondary;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean moveQuietModeOperationsToSeparateThread() {
        if (!isCached) {
            init();
        }
        return moveQuietModeOperationsToSeparateThread;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean moveSetScreenLockDialogToSettingsApp() {
        if (!isCached) {
            init();
        }
        return moveSetScreenLockDialogToSettingsApp;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean multiuserWidget() {
        if (!isCached) {
            init();
        }
        return multiuserWidget;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean perfettoMultiuserTable() {
        if (!isCached) {
            init();
        }
        return perfettoMultiuserTable;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean privateSpaceFileLimitCheckTimeout() {
        if (!isCached) {
            init();
        }
        return privateSpaceFileLimitCheckTimeout;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean profilesForAll() {
        if (!isCached) {
            init();
        }
        return profilesForAll;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean propertyInvalidatedCacheBypassMismatchedUids() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean reorderWallpaperDuringUserSwitch() {
        if (!isCached) {
            init();
        }
        return reorderWallpaperDuringUserSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean rescheduleStopIfVisibleActivities() {
        if (!isCached) {
            init();
        }
        return rescheduleStopIfVisibleActivities;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean restrictQuietModeCredentialBugFixToManagedProfiles() {
        if (!isCached) {
            init();
        }
        return restrictQuietModeCredentialBugFixToManagedProfiles;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean saveGlobalAndGuestRestrictionsOnSystemUserXml() {
        if (!isCached) {
            init();
        }
        return saveGlobalAndGuestRestrictionsOnSystemUserXml;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean saveGlobalAndGuestRestrictionsOnSystemUserXmlReadOnly() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scheduleStopOfBackgroundUser() {
        if (!isCached) {
            init();
        }
        return scheduleStopOfBackgroundUser;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean scheduleStopOfBackgroundUserByDefault() {
        if (!isCached) {
            init();
        }
        return scheduleStopOfBackgroundUserByDefault;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean setPowerModeDuringUserSwitch() {
        if (!isCached) {
            init();
        }
        return setPowerModeDuringUserSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean setupwizardUsernamePopulation() {
        if (!isCached) {
            init();
        }
        return setupwizardUsernamePopulation;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean stopExcessForBackgroundStarts() {
        if (!isCached) {
            init();
        }
        return stopExcessForBackgroundStarts;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean stopPreviousUserApps() {
        if (!isCached) {
            init();
        }
        return stopPreviousUserApps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean supportCommunalProfile() {
        if (!isCached) {
            init();
        }
        return supportCommunalProfile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean supportCommunalProfileNextgen() {
        if (!isCached) {
            init();
        }
        return supportCommunalProfileNextgen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean unicornModeRefactoringForHsumReadOnly() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useAllCpusDuringUserSwitch() {
        if (!isCached) {
            init();
        }
        return useAllCpusDuringUserSwitch;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean usePrivateSpaceIconInBiometricPrompt() {
        if (!isCached) {
            init();
        }
        return usePrivateSpaceIconInBiometricPrompt;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean userFilterRefactoring() {
        if (!isCached) {
            init();
        }
        return userFilterRefactoring;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean userRestrictionConfigWifiSharedPrivate() {
        if (!isCached) {
            init();
        }
        return userRestrictionConfigWifiSharedPrivate;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean widgetCurrentUserView() {
        if (!isCached) {
            init();
        }
        return widgetCurrentUserView;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean widgetScalingBugfix() {
        if (!isCached) {
            init();
        }
        return widgetScalingBugfix;
    }

}
