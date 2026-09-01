package android.companion;


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

    public boolean associationDeviceIcon() {
        return getValue(Flags.FLAG_ASSOCIATION_DEVICE_ICON,
            FeatureFlags::associationDeviceIcon);
    }

    @Override

    public boolean associationFailureCode() {
        return getValue(Flags.FLAG_ASSOCIATION_FAILURE_CODE,
            FeatureFlags::associationFailureCode);
    }

    @Override

    public boolean associationTag() {
        return getValue(Flags.FLAG_ASSOCIATION_TAG,
            FeatureFlags::associationTag);
    }

    @Override

    public boolean associationVerification() {
        return getValue(Flags.FLAG_ASSOCIATION_VERIFICATION,
            FeatureFlags::associationVerification);
    }

    @Override

    public boolean bandDeviceProfile() {
        return getValue(Flags.FLAG_BAND_DEVICE_PROFILE,
            FeatureFlags::bandDeviceProfile);
    }

    @Override

    public boolean devicePresence() {
        return getValue(Flags.FLAG_DEVICE_PRESENCE,
            FeatureFlags::devicePresence);
    }

    @Override

    public boolean enableDataSync() {
        return getValue(Flags.FLAG_ENABLE_DATA_SYNC,
            FeatureFlags::enableDataSync);
    }

    @Override

    public boolean enableMedicalProfile() {
        return getValue(Flags.FLAG_ENABLE_MEDICAL_PROFILE,
            FeatureFlags::enableMedicalProfile);
    }

    @Override

    public boolean enableRemoteAppAccess() {
        return getValue(Flags.FLAG_ENABLE_REMOTE_APP_ACCESS,
            FeatureFlags::enableRemoteAppAccess);
    }

    @Override

    public boolean enableTaskContinuity() {
        return getValue(Flags.FLAG_ENABLE_TASK_CONTINUITY,
            FeatureFlags::enableTaskContinuity);
    }

    @Override

    public boolean enableUniversalClipboard() {
        return getValue(Flags.FLAG_ENABLE_UNIVERSAL_CLIPBOARD,
            FeatureFlags::enableUniversalClipboard);
    }

    @Override

    public boolean newAssociationBuilder() {
        return getValue(Flags.FLAG_NEW_ASSOCIATION_BUILDER,
            FeatureFlags::newAssociationBuilder);
    }

    @Override

    public boolean notifyAssociationRemoved() {
        return getValue(Flags.FLAG_NOTIFY_ASSOCIATION_REMOVED,
            FeatureFlags::notifyAssociationRemoved);
    }

    @Override

    public boolean ongoingPermSync() {
        return getValue(Flags.FLAG_ONGOING_PERM_SYNC,
            FeatureFlags::ongoingPermSync);
    }

    @Override

    public boolean permSyncUserConsent() {
        return getValue(Flags.FLAG_PERM_SYNC_USER_CONSENT,
            FeatureFlags::permSyncUserConsent);
    }

    @Override

    public boolean unpairAssociatedDevice() {
        return getValue(Flags.FLAG_UNPAIR_ASSOCIATED_DEVICE,
            FeatureFlags::unpairAssociatedDevice);
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
            Flags.FLAG_ASSOCIATION_DEVICE_ICON,
            Flags.FLAG_ASSOCIATION_FAILURE_CODE,
            Flags.FLAG_ASSOCIATION_TAG,
            Flags.FLAG_ASSOCIATION_VERIFICATION,
            Flags.FLAG_BAND_DEVICE_PROFILE,
            Flags.FLAG_DEVICE_PRESENCE,
            Flags.FLAG_ENABLE_DATA_SYNC,
            Flags.FLAG_ENABLE_MEDICAL_PROFILE,
            Flags.FLAG_ENABLE_REMOTE_APP_ACCESS,
            Flags.FLAG_ENABLE_TASK_CONTINUITY,
            Flags.FLAG_ENABLE_UNIVERSAL_CLIPBOARD,
            Flags.FLAG_NEW_ASSOCIATION_BUILDER,
            Flags.FLAG_NOTIFY_ASSOCIATION_REMOVED,
            Flags.FLAG_ONGOING_PERM_SYNC,
            Flags.FLAG_PERM_SYNC_USER_CONSENT,
            Flags.FLAG_UNPAIR_ASSOCIATED_DEVICE
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            ""
        )
    );
}
