package android.hardware.devicestate.feature.flags;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean desktopDeviceStatePropertyApi = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.hardware.devicestate.feature.flags", 0x3F64D302A964F0F9L);
            desktopDeviceStatePropertyApi = reader.getBooleanFlagValue(0);
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

    public boolean desktopDeviceStatePropertyApi() {
        if (!isCached) {
            init();
        }
        return desktopDeviceStatePropertyApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceStateConfigurationFlag() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceStatePropertyApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceStatePropertyMigration() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceStateRdmV2() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceStateRequesterCancelState() {
        return true;
    }

}
