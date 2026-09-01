package android.hardware.devicestate.feature.flags;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean desktopDeviceStatePropertyApi();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceStateConfigurationFlag();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceStatePropertyApi();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceStatePropertyMigration();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceStateRdmV2();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceStateRequesterCancelState();
}
