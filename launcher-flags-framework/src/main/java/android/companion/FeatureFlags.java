package android.companion;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean associationDeviceIcon();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean associationFailureCode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean associationTag();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean associationVerification();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bandDeviceProfile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean devicePresence();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableDataSync();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableMedicalProfile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableRemoteAppAccess();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableTaskContinuity();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableUniversalClipboard();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean newAssociationBuilder();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notifyAssociationRemoved();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ongoingPermSync();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean permSyncUserConsent();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean unpairAssociatedDevice();
}
