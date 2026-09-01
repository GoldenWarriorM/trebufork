package android.companion.virtualdevice.flags;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean activityControlApi = true;
    private static boolean automatedAppLaunchInterception = false;
    private static boolean cameraMultipleInputStreams = true;
    private static boolean cameraTimestampFromSurface = true;
    private static boolean computerControlAccess = true;
    private static boolean computerControlActivityPolicyStrict = false;
    private static boolean computerControlConsent = false;
    private static boolean computerControlTyping = true;
    private static boolean correctVirtualDisplayPowerState = true;
    private static boolean defaultDeviceCameraAccessPolicy = true;
    private static boolean deviceAwareDisplayPower = true;
    private static boolean deviceAwareSettingsOverride = true;
    private static boolean deviceAwareUiMode = true;
    private static boolean enableAnimationsPerDisplay = true;
    private static boolean enableLimitedVdmRole = true;
    private static boolean externalCameraDefaultPolicy = true;
    private static boolean externalVirtualCameras = true;
    private static boolean fixVdmOptOutOnMirrorDisplays = false;
    private static boolean handleInvalidDeviceId = true;
    private static boolean highResolutionScroll = true;
    private static boolean itemizedVdmPermissions = true;
    private static boolean notificationsForDeviceStreaming = true;
    private static boolean statusBarAndInsets = true;
    private static boolean vdmMirrorDisplayPermission = true;
    private static boolean viewconfigurationApis = true;
    private static boolean virtualCameraMetadata = true;
    private static boolean virtualCameraNoFrameDuplication = true;
    private static boolean virtualCameraOnOpen = true;
    private static boolean virtualDisplayInsets = true;
    private static boolean virtualDisplayRotationApi = true;
    private static boolean virtualRotary = true;
    private static boolean virtualSensorAdditionalInfo = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.companion.virtualdevice.flags", 0x2A1A67697DD5E7CEL);
            activityControlApi = reader.getBooleanFlagValue(0);
            automatedAppLaunchInterception = reader.getBooleanFlagValue(1);
            cameraMultipleInputStreams = reader.getBooleanFlagValue(2);
            cameraTimestampFromSurface = reader.getBooleanFlagValue(3);
            computerControlAccess = reader.getBooleanFlagValue(4);
            computerControlActivityPolicyStrict = reader.getBooleanFlagValue(5);
            computerControlConsent = reader.getBooleanFlagValue(6);
            computerControlTyping = reader.getBooleanFlagValue(7);
            correctVirtualDisplayPowerState = reader.getBooleanFlagValue(8);
            defaultDeviceCameraAccessPolicy = reader.getBooleanFlagValue(9);
            deviceAwareDisplayPower = reader.getBooleanFlagValue(10);
            deviceAwareSettingsOverride = reader.getBooleanFlagValue(11);
            deviceAwareUiMode = reader.getBooleanFlagValue(12);
            enableAnimationsPerDisplay = reader.getBooleanFlagValue(13);
            enableLimitedVdmRole = reader.getBooleanFlagValue(14);
            externalCameraDefaultPolicy = reader.getBooleanFlagValue(15);
            externalVirtualCameras = reader.getBooleanFlagValue(16);
            fixVdmOptOutOnMirrorDisplays = reader.getBooleanFlagValue(17);
            handleInvalidDeviceId = reader.getBooleanFlagValue(18);
            highResolutionScroll = reader.getBooleanFlagValue(19);
            itemizedVdmPermissions = reader.getBooleanFlagValue(20);
            notificationsForDeviceStreaming = reader.getBooleanFlagValue(22);
            statusBarAndInsets = reader.getBooleanFlagValue(23);
            vdmMirrorDisplayPermission = reader.getBooleanFlagValue(24);
            viewconfigurationApis = reader.getBooleanFlagValue(25);
            virtualCameraMetadata = reader.getBooleanFlagValue(26);
            virtualCameraNoFrameDuplication = reader.getBooleanFlagValue(27);
            virtualCameraOnOpen = reader.getBooleanFlagValue(28);
            virtualDisplayInsets = reader.getBooleanFlagValue(29);
            virtualDisplayRotationApi = reader.getBooleanFlagValue(30);
            virtualRotary = reader.getBooleanFlagValue(31);
            virtualSensorAdditionalInfo = reader.getBooleanFlagValue(32);
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

    public boolean activityControlApi() {
        if (!isCached) {
            init();
        }
        return activityControlApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean automatedAppLaunchInterception() {
        if (!isCached) {
            init();
        }
        return automatedAppLaunchInterception;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cameraMultipleInputStreams() {
        if (!isCached) {
            init();
        }
        return cameraMultipleInputStreams;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cameraTimestampFromSurface() {
        if (!isCached) {
            init();
        }
        return cameraTimestampFromSurface;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean computerControlAccess() {
        if (!isCached) {
            init();
        }
        return computerControlAccess;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean computerControlActivityPolicyStrict() {
        if (!isCached) {
            init();
        }
        return computerControlActivityPolicyStrict;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean computerControlConsent() {
        if (!isCached) {
            init();
        }
        return computerControlConsent;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean computerControlTyping() {
        if (!isCached) {
            init();
        }
        return computerControlTyping;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean correctVirtualDisplayPowerState() {
        if (!isCached) {
            init();
        }
        return correctVirtualDisplayPowerState;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean defaultDeviceCameraAccessPolicy() {
        if (!isCached) {
            init();
        }
        return defaultDeviceCameraAccessPolicy;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceAwareDisplayPower() {
        if (!isCached) {
            init();
        }
        return deviceAwareDisplayPower;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceAwareSettingsOverride() {
        if (!isCached) {
            init();
        }
        return deviceAwareSettingsOverride;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean deviceAwareUiMode() {
        if (!isCached) {
            init();
        }
        return deviceAwareUiMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAnimationsPerDisplay() {
        if (!isCached) {
            init();
        }
        return enableAnimationsPerDisplay;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableLimitedVdmRole() {
        if (!isCached) {
            init();
        }
        return enableLimitedVdmRole;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean externalCameraDefaultPolicy() {
        if (!isCached) {
            init();
        }
        return externalCameraDefaultPolicy;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean externalVirtualCameras() {
        if (!isCached) {
            init();
        }
        return externalVirtualCameras;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean fixVdmOptOutOnMirrorDisplays() {
        if (!isCached) {
            init();
        }
        return fixVdmOptOutOnMirrorDisplays;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean handleInvalidDeviceId() {
        if (!isCached) {
            init();
        }
        return handleInvalidDeviceId;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean highResolutionScroll() {
        if (!isCached) {
            init();
        }
        return highResolutionScroll;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean itemizedVdmPermissions() {
        if (!isCached) {
            init();
        }
        return itemizedVdmPermissions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean migrateViewconfigurationConstantsToResources() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notificationsForDeviceStreaming() {
        if (!isCached) {
            init();
        }
        return notificationsForDeviceStreaming;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean statusBarAndInsets() {
        if (!isCached) {
            init();
        }
        return statusBarAndInsets;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean vdmMirrorDisplayPermission() {
        if (!isCached) {
            init();
        }
        return vdmMirrorDisplayPermission;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean viewconfigurationApis() {
        if (!isCached) {
            init();
        }
        return viewconfigurationApis;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualCameraMetadata() {
        if (!isCached) {
            init();
        }
        return virtualCameraMetadata;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualCameraNoFrameDuplication() {
        if (!isCached) {
            init();
        }
        return virtualCameraNoFrameDuplication;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualCameraOnOpen() {
        if (!isCached) {
            init();
        }
        return virtualCameraOnOpen;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualDisplayInsets() {
        if (!isCached) {
            init();
        }
        return virtualDisplayInsets;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualDisplayRotationApi() {
        if (!isCached) {
            init();
        }
        return virtualDisplayRotationApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualRotary() {
        if (!isCached) {
            init();
        }
        return virtualRotary;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean virtualSensorAdditionalInfo() {
        if (!isCached) {
            init();
        }
        return virtualSensorAdditionalInfo;
    }

}
