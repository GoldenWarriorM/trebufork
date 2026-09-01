package android.multiuser;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
/** @hide */
public class CustomFeatureFlags implements FeatureFlags {

    private BiPredicate<String, Predicate<FeatureFlags>> mGetValueImpl;

    public CustomFeatureFlags(BiPredicate<String, Predicate<FeatureFlags>> getValueImpl) {
        mGetValueImpl = getValueImpl;
    }
    @Override

    public boolean addLauncherUserConfig() {
        return getValue(Flags.FLAG_ADD_LAUNCHER_USER_CONFIG,
            FeatureFlags::addLauncherUserConfig);
    }

    @Override

    public boolean allowMainUserToAccessBlockedNumberProvider() {
        return getValue(Flags.FLAG_ALLOW_MAIN_USER_TO_ACCESS_BLOCKED_NUMBER_PROVIDER,
            FeatureFlags::allowMainUserToAccessBlockedNumberProvider);
    }

    @Override

    public boolean allowResolverSheetForPrivateSpace() {
        return getValue(Flags.FLAG_ALLOW_RESOLVER_SHEET_FOR_PRIVATE_SPACE,
            FeatureFlags::allowResolverSheetForPrivateSpace);
    }

    @Override

    public boolean allowSupervisingProfile() {
        return getValue(Flags.FLAG_ALLOW_SUPERVISING_PROFILE,
            FeatureFlags::allowSupervisingProfile);
    }

    @Override

    public boolean backupActivatedForAllUsers() {
        return getValue(Flags.FLAG_BACKUP_ACTIVATED_FOR_ALL_USERS,
            FeatureFlags::backupActivatedForAllUsers);
    }

    @Override

    public boolean bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch() {
        return getValue(Flags.FLAG_BIND_WALLPAPER_SERVICE_ON_ITS_OWN_THREAD_DURING_A_USER_SWITCH,
            FeatureFlags::bindWallpaperServiceOnItsOwnThreadDuringAUserSwitch);
    }

    @Override

    public boolean blockPrivateSpaceCreation() {
        return getValue(Flags.FLAG_BLOCK_PRIVATE_SPACE_CREATION,
            FeatureFlags::blockPrivateSpaceCreation);
    }

    @Override

    public boolean cacheProfileTypeReadOnly() {
        return getValue(Flags.FLAG_CACHE_PROFILE_TYPE_READ_ONLY,
            FeatureFlags::cacheProfileTypeReadOnly);
    }

    @Override

    public boolean cacheUserRestrictionsReadOnly() {
        return getValue(Flags.FLAG_CACHE_USER_RESTRICTIONS_READ_ONLY,
            FeatureFlags::cacheUserRestrictionsReadOnly);
    }

    @Override

    public boolean cacheUserStartRealtimeReadOnly() {
        return getValue(Flags.FLAG_CACHE_USER_START_REALTIME_READ_ONLY,
            FeatureFlags::cacheUserStartRealtimeReadOnly);
    }

    @Override

    public boolean cacheUserUnlockRealtimeReadOnly() {
        return getValue(Flags.FLAG_CACHE_USER_UNLOCK_REALTIME_READ_ONLY,
            FeatureFlags::cacheUserUnlockRealtimeReadOnly);
    }

    @Override

    public boolean consistentMaxUsers() {
        return getValue(Flags.FLAG_CONSISTENT_MAX_USERS,
            FeatureFlags::consistentMaxUsers);
    }

    @Override

    public boolean createInitialUser() {
        return getValue(Flags.FLAG_CREATE_INITIAL_USER,
            FeatureFlags::createInitialUser);
    }

    @Override

    public boolean decoupleMaxUsersFromProfiles() {
        return getValue(Flags.FLAG_DECOUPLE_MAX_USERS_FROM_PROFILES,
            FeatureFlags::decoupleMaxUsersFromProfiles);
    }

    @Override

    public boolean demoteMainUser() {
        return getValue(Flags.FLAG_DEMOTE_MAIN_USER,
            FeatureFlags::demoteMainUser);
    }

    @Override

    public boolean disablePrivateSpaceItemsOnHome() {
        return getValue(Flags.FLAG_DISABLE_PRIVATE_SPACE_ITEMS_ON_HOME,
            FeatureFlags::disablePrivateSpaceItemsOnHome);
    }

    @Override

    public boolean disallowRemovingLastAdminUser() {
        return getValue(Flags.FLAG_DISALLOW_REMOVING_LAST_ADMIN_USER,
            FeatureFlags::disallowRemovingLastAdminUser);
    }

    @Override

    public boolean enableBiometricsToUnlockPrivateSpace() {
        return getValue(Flags.FLAG_ENABLE_BIOMETRICS_TO_UNLOCK_PRIVATE_SPACE,
            FeatureFlags::enableBiometricsToUnlockPrivateSpace);
    }

    @Override

    public boolean enableHidingProfiles() {
        return getValue(Flags.FLAG_ENABLE_HIDING_PROFILES,
            FeatureFlags::enableHidingProfiles);
    }

    @Override

    public boolean enableMovingContentIntoPrivateSpace() {
        return getValue(Flags.FLAG_ENABLE_MOVING_CONTENT_INTO_PRIVATE_SPACE,
            FeatureFlags::enableMovingContentIntoPrivateSpace);
    }

    @Override

    public boolean enablePermissionToAccessHiddenProfiles() {
        return getValue(Flags.FLAG_ENABLE_PERMISSION_TO_ACCESS_HIDDEN_PROFILES,
            FeatureFlags::enablePermissionToAccessHiddenProfiles);
    }

    @Override

    public boolean enablePrivateSpaceAutolockOnRestarts() {
        return getValue(Flags.FLAG_ENABLE_PRIVATE_SPACE_AUTOLOCK_ON_RESTARTS,
            FeatureFlags::enablePrivateSpaceAutolockOnRestarts);
    }

    @Override

    public boolean enablePrivateSpaceFeatures() {
        return getValue(Flags.FLAG_ENABLE_PRIVATE_SPACE_FEATURES,
            FeatureFlags::enablePrivateSpaceFeatures);
    }

    @Override

    public boolean enablePrivateSpaceIntentRedirection() {
        return getValue(Flags.FLAG_ENABLE_PRIVATE_SPACE_INTENT_REDIRECTION,
            FeatureFlags::enablePrivateSpaceIntentRedirection);
    }

    @Override

    public boolean enablePsSensitiveNotificationsToggle() {
        return getValue(Flags.FLAG_ENABLE_PS_SENSITIVE_NOTIFICATIONS_TOGGLE,
            FeatureFlags::enablePsSensitiveNotificationsToggle);
    }

    @Override

    public boolean enableSystemUserOnlyForServicesAndProviders() {
        return getValue(Flags.FLAG_ENABLE_SYSTEM_USER_ONLY_FOR_SERVICES_AND_PROVIDERS,
            FeatureFlags::enableSystemUserOnlyForServicesAndProviders);
    }

    @Override

    public boolean fixAvatarContentProviderNullAuthority() {
        return getValue(Flags.FLAG_FIX_AVATAR_CONTENT_PROVIDER_NULL_AUTHORITY,
            FeatureFlags::fixAvatarContentProviderNullAuthority);
    }

    @Override

    public boolean hsuAllowlistActivities() {
        return getValue(Flags.FLAG_HSU_ALLOWLIST_ACTIVITIES,
            FeatureFlags::hsuAllowlistActivities);
    }

    @Override

    public boolean hsuDeviceProvisioner() {
        return getValue(Flags.FLAG_HSU_DEVICE_PROVISIONER,
            FeatureFlags::hsuDeviceProvisioner);
    }

    @Override

    public boolean hsuNotAdmin() {
        return getValue(Flags.FLAG_HSU_NOT_ADMIN,
            FeatureFlags::hsuNotAdmin);
    }

    @Override

    public boolean logoutUserApi() {
        return getValue(Flags.FLAG_LOGOUT_USER_API,
            FeatureFlags::logoutUserApi);
    }

    @Override

    public boolean maxUsersInCarIsForSecondary() {
        return getValue(Flags.FLAG_MAX_USERS_IN_CAR_IS_FOR_SECONDARY,
            FeatureFlags::maxUsersInCarIsForSecondary);
    }

    @Override

    public boolean moveQuietModeOperationsToSeparateThread() {
        return getValue(Flags.FLAG_MOVE_QUIET_MODE_OPERATIONS_TO_SEPARATE_THREAD,
            FeatureFlags::moveQuietModeOperationsToSeparateThread);
    }

    @Override

    public boolean moveSetScreenLockDialogToSettingsApp() {
        return getValue(Flags.FLAG_MOVE_SET_SCREEN_LOCK_DIALOG_TO_SETTINGS_APP,
            FeatureFlags::moveSetScreenLockDialogToSettingsApp);
    }

    @Override

    public boolean multiuserWidget() {
        return getValue(Flags.FLAG_MULTIUSER_WIDGET,
            FeatureFlags::multiuserWidget);
    }

    @Override

    public boolean perfettoMultiuserTable() {
        return getValue(Flags.FLAG_PERFETTO_MULTIUSER_TABLE,
            FeatureFlags::perfettoMultiuserTable);
    }

    @Override

    public boolean privateSpaceFileLimitCheckTimeout() {
        return getValue(Flags.FLAG_PRIVATE_SPACE_FILE_LIMIT_CHECK_TIMEOUT,
            FeatureFlags::privateSpaceFileLimitCheckTimeout);
    }

    @Override

    public boolean profilesForAll() {
        return getValue(Flags.FLAG_PROFILES_FOR_ALL,
            FeatureFlags::profilesForAll);
    }

    @Override

    public boolean propertyInvalidatedCacheBypassMismatchedUids() {
        return getValue(Flags.FLAG_PROPERTY_INVALIDATED_CACHE_BYPASS_MISMATCHED_UIDS,
            FeatureFlags::propertyInvalidatedCacheBypassMismatchedUids);
    }

    @Override

    public boolean reorderWallpaperDuringUserSwitch() {
        return getValue(Flags.FLAG_REORDER_WALLPAPER_DURING_USER_SWITCH,
            FeatureFlags::reorderWallpaperDuringUserSwitch);
    }

    @Override

    public boolean rescheduleStopIfVisibleActivities() {
        return getValue(Flags.FLAG_RESCHEDULE_STOP_IF_VISIBLE_ACTIVITIES,
            FeatureFlags::rescheduleStopIfVisibleActivities);
    }

    @Override

    public boolean restrictQuietModeCredentialBugFixToManagedProfiles() {
        return getValue(Flags.FLAG_RESTRICT_QUIET_MODE_CREDENTIAL_BUG_FIX_TO_MANAGED_PROFILES,
            FeatureFlags::restrictQuietModeCredentialBugFixToManagedProfiles);
    }

    @Override

    public boolean saveGlobalAndGuestRestrictionsOnSystemUserXml() {
        return getValue(Flags.FLAG_SAVE_GLOBAL_AND_GUEST_RESTRICTIONS_ON_SYSTEM_USER_XML,
            FeatureFlags::saveGlobalAndGuestRestrictionsOnSystemUserXml);
    }

    @Override

    public boolean saveGlobalAndGuestRestrictionsOnSystemUserXmlReadOnly() {
        return getValue(Flags.FLAG_SAVE_GLOBAL_AND_GUEST_RESTRICTIONS_ON_SYSTEM_USER_XML_READ_ONLY,
            FeatureFlags::saveGlobalAndGuestRestrictionsOnSystemUserXmlReadOnly);
    }

    @Override

    public boolean scheduleStopOfBackgroundUser() {
        return getValue(Flags.FLAG_SCHEDULE_STOP_OF_BACKGROUND_USER,
            FeatureFlags::scheduleStopOfBackgroundUser);
    }

    @Override

    public boolean scheduleStopOfBackgroundUserByDefault() {
        return getValue(Flags.FLAG_SCHEDULE_STOP_OF_BACKGROUND_USER_BY_DEFAULT,
            FeatureFlags::scheduleStopOfBackgroundUserByDefault);
    }

    @Override

    public boolean setPowerModeDuringUserSwitch() {
        return getValue(Flags.FLAG_SET_POWER_MODE_DURING_USER_SWITCH,
            FeatureFlags::setPowerModeDuringUserSwitch);
    }

    @Override

    public boolean setupwizardUsernamePopulation() {
        return getValue(Flags.FLAG_SETUPWIZARD_USERNAME_POPULATION,
            FeatureFlags::setupwizardUsernamePopulation);
    }

    @Override

    public boolean stopExcessForBackgroundStarts() {
        return getValue(Flags.FLAG_STOP_EXCESS_FOR_BACKGROUND_STARTS,
            FeatureFlags::stopExcessForBackgroundStarts);
    }

    @Override

    public boolean stopPreviousUserApps() {
        return getValue(Flags.FLAG_STOP_PREVIOUS_USER_APPS,
            FeatureFlags::stopPreviousUserApps);
    }

    @Override

    public boolean supportCommunalProfile() {
        return getValue(Flags.FLAG_SUPPORT_COMMUNAL_PROFILE,
            FeatureFlags::supportCommunalProfile);
    }

    @Override

    public boolean supportCommunalProfileNextgen() {
        return getValue(Flags.FLAG_SUPPORT_COMMUNAL_PROFILE_NEXTGEN,
            FeatureFlags::supportCommunalProfileNextgen);
    }

    @Override

    public boolean unicornModeRefactoringForHsumReadOnly() {
        return getValue(Flags.FLAG_UNICORN_MODE_REFACTORING_FOR_HSUM_READ_ONLY,
            FeatureFlags::unicornModeRefactoringForHsumReadOnly);
    }

    @Override

    public boolean useAllCpusDuringUserSwitch() {
        return getValue(Flags.FLAG_USE_ALL_CPUS_DURING_USER_SWITCH,
            FeatureFlags::useAllCpusDuringUserSwitch);
    }

    @Override

    public boolean usePrivateSpaceIconInBiometricPrompt() {
        return getValue(Flags.FLAG_USE_PRIVATE_SPACE_ICON_IN_BIOMETRIC_PROMPT,
            FeatureFlags::usePrivateSpaceIconInBiometricPrompt);
    }

    @Override

    public boolean userFilterRefactoring() {
        return getValue(Flags.FLAG_USER_FILTER_REFACTORING,
            FeatureFlags::userFilterRefactoring);
    }

    @Override

    public boolean userRestrictionConfigWifiSharedPrivate() {
        return getValue(Flags.FLAG_USER_RESTRICTION_CONFIG_WIFI_SHARED_PRIVATE,
            FeatureFlags::userRestrictionConfigWifiSharedPrivate);
    }

    @Override

    public boolean widgetCurrentUserView() {
        return getValue(Flags.FLAG_WIDGET_CURRENT_USER_VIEW,
            FeatureFlags::widgetCurrentUserView);
    }

    @Override

    public boolean widgetScalingBugfix() {
        return getValue(Flags.FLAG_WIDGET_SCALING_BUGFIX,
            FeatureFlags::widgetScalingBugfix);
    }

    public boolean isFlagReadOnlyOptimized(String flagName) {
        if (mReadOnlyFlagsSet.contains(flagName) &&
            isOptimizationEnabled()) {
                return true;
        }
        return false;
    }

    @com.android.aconfig.annotations.AssumeTrueForR8
    private boolean isOptimizationEnabled() {
        return false;
    }

    protected boolean getValue(String flagName, Predicate<FeatureFlags> getter) {
        return mGetValueImpl.test(flagName, getter);
    }

    public List<String> getFlagNames() {
        return Arrays.asList(
            Flags.FLAG_ADD_LAUNCHER_USER_CONFIG,
            Flags.FLAG_ALLOW_MAIN_USER_TO_ACCESS_BLOCKED_NUMBER_PROVIDER,
            Flags.FLAG_ALLOW_RESOLVER_SHEET_FOR_PRIVATE_SPACE,
            Flags.FLAG_ALLOW_SUPERVISING_PROFILE,
            Flags.FLAG_BACKUP_ACTIVATED_FOR_ALL_USERS,
            Flags.FLAG_BIND_WALLPAPER_SERVICE_ON_ITS_OWN_THREAD_DURING_A_USER_SWITCH,
            Flags.FLAG_BLOCK_PRIVATE_SPACE_CREATION,
            Flags.FLAG_CACHE_PROFILE_TYPE_READ_ONLY,
            Flags.FLAG_CACHE_USER_RESTRICTIONS_READ_ONLY,
            Flags.FLAG_CACHE_USER_START_REALTIME_READ_ONLY,
            Flags.FLAG_CACHE_USER_UNLOCK_REALTIME_READ_ONLY,
            Flags.FLAG_CONSISTENT_MAX_USERS,
            Flags.FLAG_CREATE_INITIAL_USER,
            Flags.FLAG_DECOUPLE_MAX_USERS_FROM_PROFILES,
            Flags.FLAG_DEMOTE_MAIN_USER,
            Flags.FLAG_DISABLE_PRIVATE_SPACE_ITEMS_ON_HOME,
            Flags.FLAG_DISALLOW_REMOVING_LAST_ADMIN_USER,
            Flags.FLAG_ENABLE_BIOMETRICS_TO_UNLOCK_PRIVATE_SPACE,
            Flags.FLAG_ENABLE_HIDING_PROFILES,
            Flags.FLAG_ENABLE_MOVING_CONTENT_INTO_PRIVATE_SPACE,
            Flags.FLAG_ENABLE_PERMISSION_TO_ACCESS_HIDDEN_PROFILES,
            Flags.FLAG_ENABLE_PRIVATE_SPACE_AUTOLOCK_ON_RESTARTS,
            Flags.FLAG_ENABLE_PRIVATE_SPACE_FEATURES,
            Flags.FLAG_ENABLE_PRIVATE_SPACE_INTENT_REDIRECTION,
            Flags.FLAG_ENABLE_PS_SENSITIVE_NOTIFICATIONS_TOGGLE,
            Flags.FLAG_ENABLE_SYSTEM_USER_ONLY_FOR_SERVICES_AND_PROVIDERS,
            Flags.FLAG_FIX_AVATAR_CONTENT_PROVIDER_NULL_AUTHORITY,
            Flags.FLAG_HSU_ALLOWLIST_ACTIVITIES,
            Flags.FLAG_HSU_DEVICE_PROVISIONER,
            Flags.FLAG_HSU_NOT_ADMIN,
            Flags.FLAG_LOGOUT_USER_API,
            Flags.FLAG_MAX_USERS_IN_CAR_IS_FOR_SECONDARY,
            Flags.FLAG_MOVE_QUIET_MODE_OPERATIONS_TO_SEPARATE_THREAD,
            Flags.FLAG_MOVE_SET_SCREEN_LOCK_DIALOG_TO_SETTINGS_APP,
            Flags.FLAG_MULTIUSER_WIDGET,
            Flags.FLAG_PERFETTO_MULTIUSER_TABLE,
            Flags.FLAG_PRIVATE_SPACE_FILE_LIMIT_CHECK_TIMEOUT,
            Flags.FLAG_PROFILES_FOR_ALL,
            Flags.FLAG_PROPERTY_INVALIDATED_CACHE_BYPASS_MISMATCHED_UIDS,
            Flags.FLAG_REORDER_WALLPAPER_DURING_USER_SWITCH,
            Flags.FLAG_RESCHEDULE_STOP_IF_VISIBLE_ACTIVITIES,
            Flags.FLAG_RESTRICT_QUIET_MODE_CREDENTIAL_BUG_FIX_TO_MANAGED_PROFILES,
            Flags.FLAG_SAVE_GLOBAL_AND_GUEST_RESTRICTIONS_ON_SYSTEM_USER_XML,
            Flags.FLAG_SAVE_GLOBAL_AND_GUEST_RESTRICTIONS_ON_SYSTEM_USER_XML_READ_ONLY,
            Flags.FLAG_SCHEDULE_STOP_OF_BACKGROUND_USER,
            Flags.FLAG_SCHEDULE_STOP_OF_BACKGROUND_USER_BY_DEFAULT,
            Flags.FLAG_SET_POWER_MODE_DURING_USER_SWITCH,
            Flags.FLAG_SETUPWIZARD_USERNAME_POPULATION,
            Flags.FLAG_STOP_EXCESS_FOR_BACKGROUND_STARTS,
            Flags.FLAG_STOP_PREVIOUS_USER_APPS,
            Flags.FLAG_SUPPORT_COMMUNAL_PROFILE,
            Flags.FLAG_SUPPORT_COMMUNAL_PROFILE_NEXTGEN,
            Flags.FLAG_UNICORN_MODE_REFACTORING_FOR_HSUM_READ_ONLY,
            Flags.FLAG_USE_ALL_CPUS_DURING_USER_SWITCH,
            Flags.FLAG_USE_PRIVATE_SPACE_ICON_IN_BIOMETRIC_PROMPT,
            Flags.FLAG_USER_FILTER_REFACTORING,
            Flags.FLAG_USER_RESTRICTION_CONFIG_WIFI_SHARED_PRIVATE,
            Flags.FLAG_WIDGET_CURRENT_USER_VIEW,
            Flags.FLAG_WIDGET_SCALING_BUGFIX
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            Flags.FLAG_BACKUP_ACTIVATED_FOR_ALL_USERS,
            Flags.FLAG_BLOCK_PRIVATE_SPACE_CREATION,
            Flags.FLAG_CACHE_PROFILE_TYPE_READ_ONLY,
            Flags.FLAG_CACHE_USER_RESTRICTIONS_READ_ONLY,
            Flags.FLAG_CACHE_USER_START_REALTIME_READ_ONLY,
            Flags.FLAG_CACHE_USER_UNLOCK_REALTIME_READ_ONLY,
            Flags.FLAG_ENABLE_SYSTEM_USER_ONLY_FOR_SERVICES_AND_PROVIDERS,
            Flags.FLAG_PROPERTY_INVALIDATED_CACHE_BYPASS_MISMATCHED_UIDS,
            Flags.FLAG_SAVE_GLOBAL_AND_GUEST_RESTRICTIONS_ON_SYSTEM_USER_XML_READ_ONLY,
            Flags.FLAG_UNICORN_MODE_REFACTORING_FOR_HSUM_READ_ONLY,
            ""
        )
    );
}
