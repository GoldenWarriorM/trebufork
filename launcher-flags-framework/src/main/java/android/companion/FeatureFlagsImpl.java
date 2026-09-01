package android.companion;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean associationDeviceIcon = true;
    private static boolean associationFailureCode = true;
    private static boolean associationTag = true;
    private static boolean associationVerification = true;
    private static boolean bandDeviceProfile = true;
    private static boolean devicePresence = true;
    private static boolean enableDataSync = false;
    private static boolean enableMedicalProfile = true;
    private static boolean enableRemoteAppAccess = false;
    private static boolean enableTaskContinuity = false;
    private static boolean enableUniversalClipboard = false;
    private static boolean newAssociationBuilder = true;
    private static boolean notifyAssociationRemoved = true;
    private static boolean ongoingPermSync = false;
    private static boolean permSyncUserConsent = true;
    private static boolean unpairAssociatedDevice = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.companion", 0x559944332567715FL);
            associationDeviceIcon = reader.getBooleanFlagValue(0);
            associationFailureCode = reader.getBooleanFlagValue(1);
            associationTag = reader.getBooleanFlagValue(2);
            associationVerification = reader.getBooleanFlagValue(3);
            bandDeviceProfile = reader.getBooleanFlagValue(4);
            devicePresence = reader.getBooleanFlagValue(5);
            enableDataSync = reader.getBooleanFlagValue(6);
            enableMedicalProfile = reader.getBooleanFlagValue(7);
            newAssociationBuilder = reader.getBooleanFlagValue(11);
            notifyAssociationRemoved = reader.getBooleanFlagValue(12);
            ongoingPermSync = reader.getBooleanFlagValue(13);
            permSyncUserConsent = reader.getBooleanFlagValue(14);
            unpairAssociatedDevice = reader.getBooleanFlagValue(15);
            enableRemoteAppAccess = reader.getBooleanFlagValue(8);
            enableTaskContinuity = reader.getBooleanFlagValue(9);
            enableUniversalClipboard = reader.getBooleanFlagValue(10);
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

    public boolean associationDeviceIcon() {
        if (!isCached) {
            init();
        }
        return associationDeviceIcon;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean associationFailureCode() {
        if (!isCached) {
            init();
        }
        return associationFailureCode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean associationTag() {
        if (!isCached) {
            init();
        }
        return associationTag;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean associationVerification() {
        if (!isCached) {
            init();
        }
        return associationVerification;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bandDeviceProfile() {
        if (!isCached) {
            init();
        }
        return bandDeviceProfile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean devicePresence() {
        if (!isCached) {
            init();
        }
        return devicePresence;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableDataSync() {
        if (!isCached) {
            init();
        }
        return enableDataSync;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableMedicalProfile() {
        if (!isCached) {
            init();
        }
        return enableMedicalProfile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableRemoteAppAccess() {
        if (!isCached) {
            init();
        }
        return enableRemoteAppAccess;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableTaskContinuity() {
        if (!isCached) {
            init();
        }
        return enableTaskContinuity;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableUniversalClipboard() {
        if (!isCached) {
            init();
        }
        return enableUniversalClipboard;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean newAssociationBuilder() {
        if (!isCached) {
            init();
        }
        return newAssociationBuilder;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notifyAssociationRemoved() {
        if (!isCached) {
            init();
        }
        return notifyAssociationRemoved;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ongoingPermSync() {
        if (!isCached) {
            init();
        }
        return ongoingPermSync;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean permSyncUserConsent() {
        if (!isCached) {
            init();
        }
        return permSyncUserConsent;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean unpairAssociatedDevice() {
        if (!isCached) {
            init();
        }
        return unpairAssociatedDevice;
    }

}
