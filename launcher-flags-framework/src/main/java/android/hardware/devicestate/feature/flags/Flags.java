package android.hardware.devicestate.feature.flags;


/** @hide */
public final class Flags {
    /** @hide */
    public static final String FLAG_DESKTOP_DEVICE_STATE_PROPERTY_API = "android.hardware.devicestate.feature.flags.desktop_device_state_property_api";
    /** @hide */
    public static final String FLAG_DEVICE_STATE_CONFIGURATION_FLAG = "android.hardware.devicestate.feature.flags.device_state_configuration_flag";
    /** @hide */
    public static final String FLAG_DEVICE_STATE_PROPERTY_API = "android.hardware.devicestate.feature.flags.device_state_property_api";
    /** @hide */
    public static final String FLAG_DEVICE_STATE_PROPERTY_MIGRATION = "android.hardware.devicestate.feature.flags.device_state_property_migration";
    /** @hide */
    public static final String FLAG_DEVICE_STATE_RDM_V2 = "android.hardware.devicestate.feature.flags.device_state_rdm_v2";
    /** @hide */
    public static final String FLAG_DEVICE_STATE_REQUESTER_CANCEL_STATE = "android.hardware.devicestate.feature.flags.device_state_requester_cancel_state";
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean desktopDeviceStatePropertyApi() {
        
        return FEATURE_FLAGS.desktopDeviceStatePropertyApi();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean deviceStateConfigurationFlag() {
        
        return FEATURE_FLAGS.deviceStateConfigurationFlag();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean deviceStatePropertyApi() {
        
        return FEATURE_FLAGS.deviceStatePropertyApi();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean deviceStatePropertyMigration() {
        
        return FEATURE_FLAGS.deviceStatePropertyMigration();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean deviceStateRdmV2() {
        
        return FEATURE_FLAGS.deviceStateRdmV2();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean deviceStateRequesterCancelState() {
        
        return FEATURE_FLAGS.deviceStateRequesterCancelState();
    }

    private static FeatureFlags FEATURE_FLAGS = new FeatureFlagsImpl();

}
