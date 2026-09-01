package android.companion;


/** @hide */
public final class Flags {
    /** @hide */
    public static final String FLAG_ASSOCIATION_DEVICE_ICON = "android.companion.association_device_icon";
    /** @hide */
    public static final String FLAG_ASSOCIATION_FAILURE_CODE = "android.companion.association_failure_code";
    /** @hide */
    public static final String FLAG_ASSOCIATION_TAG = "android.companion.association_tag";
    /** @hide */
    public static final String FLAG_ASSOCIATION_VERIFICATION = "android.companion.association_verification";
    /** @hide */
    public static final String FLAG_BAND_DEVICE_PROFILE = "android.companion.band_device_profile";
    /** @hide */
    public static final String FLAG_DEVICE_PRESENCE = "android.companion.device_presence";
    /** @hide */
    public static final String FLAG_ENABLE_DATA_SYNC = "android.companion.enable_data_sync";
    /** @hide */
    public static final String FLAG_ENABLE_MEDICAL_PROFILE = "android.companion.enable_medical_profile";
    /** @hide */
    public static final String FLAG_ENABLE_REMOTE_APP_ACCESS = "android.companion.enable_remote_app_access";
    /** @hide */
    public static final String FLAG_ENABLE_TASK_CONTINUITY = "android.companion.enable_task_continuity";
    /** @hide */
    public static final String FLAG_ENABLE_UNIVERSAL_CLIPBOARD = "android.companion.enable_universal_clipboard";
    /** @hide */
    public static final String FLAG_NEW_ASSOCIATION_BUILDER = "android.companion.new_association_builder";
    /** @hide */
    public static final String FLAG_NOTIFY_ASSOCIATION_REMOVED = "android.companion.notify_association_removed";
    /** @hide */
    public static final String FLAG_ONGOING_PERM_SYNC = "android.companion.ongoing_perm_sync";
    /** @hide */
    public static final String FLAG_PERM_SYNC_USER_CONSENT = "android.companion.perm_sync_user_consent";
    /** @hide */
    public static final String FLAG_UNPAIR_ASSOCIATED_DEVICE = "android.companion.unpair_associated_device";
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean associationDeviceIcon() {
        
        return FEATURE_FLAGS.associationDeviceIcon();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean associationFailureCode() {
        
        return FEATURE_FLAGS.associationFailureCode();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean associationTag() {
        
        return FEATURE_FLAGS.associationTag();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean associationVerification() {
        
        return FEATURE_FLAGS.associationVerification();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean bandDeviceProfile() {
        
        return FEATURE_FLAGS.bandDeviceProfile();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean devicePresence() {
        
        return FEATURE_FLAGS.devicePresence();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean enableDataSync() {
        
        return FEATURE_FLAGS.enableDataSync();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean enableMedicalProfile() {
        
        return FEATURE_FLAGS.enableMedicalProfile();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean enableRemoteAppAccess() {
        
        return FEATURE_FLAGS.enableRemoteAppAccess();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean enableTaskContinuity() {
        
        return FEATURE_FLAGS.enableTaskContinuity();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean enableUniversalClipboard() {
        
        return FEATURE_FLAGS.enableUniversalClipboard();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean newAssociationBuilder() {
        
        return FEATURE_FLAGS.newAssociationBuilder();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean notifyAssociationRemoved() {
        
        return FEATURE_FLAGS.notifyAssociationRemoved();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean ongoingPermSync() {
        
        return FEATURE_FLAGS.ongoingPermSync();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean permSyncUserConsent() {
        
        return FEATURE_FLAGS.permSyncUserConsent();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean unpairAssociatedDevice() {
        
        return FEATURE_FLAGS.unpairAssociatedDevice();
    }

    private static FeatureFlags FEATURE_FLAGS = new FeatureFlagsImpl();

}
