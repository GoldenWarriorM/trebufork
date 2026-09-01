package android.hardware.devicestate.feature.flags;


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

    public boolean desktopDeviceStatePropertyApi() {
        return getValue(Flags.FLAG_DESKTOP_DEVICE_STATE_PROPERTY_API,
            FeatureFlags::desktopDeviceStatePropertyApi);
    }

    @Override

    public boolean deviceStateConfigurationFlag() {
        return getValue(Flags.FLAG_DEVICE_STATE_CONFIGURATION_FLAG,
            FeatureFlags::deviceStateConfigurationFlag);
    }

    @Override

    public boolean deviceStatePropertyApi() {
        return getValue(Flags.FLAG_DEVICE_STATE_PROPERTY_API,
            FeatureFlags::deviceStatePropertyApi);
    }

    @Override

    public boolean deviceStatePropertyMigration() {
        return getValue(Flags.FLAG_DEVICE_STATE_PROPERTY_MIGRATION,
            FeatureFlags::deviceStatePropertyMigration);
    }

    @Override

    public boolean deviceStateRdmV2() {
        return getValue(Flags.FLAG_DEVICE_STATE_RDM_V2,
            FeatureFlags::deviceStateRdmV2);
    }

    @Override

    public boolean deviceStateRequesterCancelState() {
        return getValue(Flags.FLAG_DEVICE_STATE_REQUESTER_CANCEL_STATE,
            FeatureFlags::deviceStateRequesterCancelState);
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
            Flags.FLAG_DESKTOP_DEVICE_STATE_PROPERTY_API,
            Flags.FLAG_DEVICE_STATE_CONFIGURATION_FLAG,
            Flags.FLAG_DEVICE_STATE_PROPERTY_API,
            Flags.FLAG_DEVICE_STATE_PROPERTY_MIGRATION,
            Flags.FLAG_DEVICE_STATE_RDM_V2,
            Flags.FLAG_DEVICE_STATE_REQUESTER_CANCEL_STATE
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            Flags.FLAG_DEVICE_STATE_CONFIGURATION_FLAG,
            Flags.FLAG_DEVICE_STATE_PROPERTY_API,
            Flags.FLAG_DEVICE_STATE_PROPERTY_MIGRATION,
            Flags.FLAG_DEVICE_STATE_RDM_V2,
            Flags.FLAG_DEVICE_STATE_REQUESTER_CANCEL_STATE,
            ""
        )
    );
}
