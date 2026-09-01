package android.companion.virtualdevice.flags;


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

    public boolean activityControlApi() {
        return getValue(Flags.FLAG_ACTIVITY_CONTROL_API,
            FeatureFlags::activityControlApi);
    }

    @Override

    public boolean automatedAppLaunchInterception() {
        return getValue(Flags.FLAG_AUTOMATED_APP_LAUNCH_INTERCEPTION,
            FeatureFlags::automatedAppLaunchInterception);
    }

    @Override

    public boolean cameraMultipleInputStreams() {
        return getValue(Flags.FLAG_CAMERA_MULTIPLE_INPUT_STREAMS,
            FeatureFlags::cameraMultipleInputStreams);
    }

    @Override

    public boolean cameraTimestampFromSurface() {
        return getValue(Flags.FLAG_CAMERA_TIMESTAMP_FROM_SURFACE,
            FeatureFlags::cameraTimestampFromSurface);
    }

    @Override

    public boolean computerControlAccess() {
        return getValue(Flags.FLAG_COMPUTER_CONTROL_ACCESS,
            FeatureFlags::computerControlAccess);
    }

    @Override

    public boolean computerControlActivityPolicyStrict() {
        return getValue(Flags.FLAG_COMPUTER_CONTROL_ACTIVITY_POLICY_STRICT,
            FeatureFlags::computerControlActivityPolicyStrict);
    }

    @Override

    public boolean computerControlConsent() {
        return getValue(Flags.FLAG_COMPUTER_CONTROL_CONSENT,
            FeatureFlags::computerControlConsent);
    }

    @Override

    public boolean computerControlTyping() {
        return getValue(Flags.FLAG_COMPUTER_CONTROL_TYPING,
            FeatureFlags::computerControlTyping);
    }

    @Override

    public boolean correctVirtualDisplayPowerState() {
        return getValue(Flags.FLAG_CORRECT_VIRTUAL_DISPLAY_POWER_STATE,
            FeatureFlags::correctVirtualDisplayPowerState);
    }

    @Override

    public boolean defaultDeviceCameraAccessPolicy() {
        return getValue(Flags.FLAG_DEFAULT_DEVICE_CAMERA_ACCESS_POLICY,
            FeatureFlags::defaultDeviceCameraAccessPolicy);
    }

    @Override

    public boolean deviceAwareDisplayPower() {
        return getValue(Flags.FLAG_DEVICE_AWARE_DISPLAY_POWER,
            FeatureFlags::deviceAwareDisplayPower);
    }

    @Override

    public boolean deviceAwareSettingsOverride() {
        return getValue(Flags.FLAG_DEVICE_AWARE_SETTINGS_OVERRIDE,
            FeatureFlags::deviceAwareSettingsOverride);
    }

    @Override

    public boolean deviceAwareUiMode() {
        return getValue(Flags.FLAG_DEVICE_AWARE_UI_MODE,
            FeatureFlags::deviceAwareUiMode);
    }

    @Override

    public boolean enableAnimationsPerDisplay() {
        return getValue(Flags.FLAG_ENABLE_ANIMATIONS_PER_DISPLAY,
            FeatureFlags::enableAnimationsPerDisplay);
    }

    @Override

    public boolean enableLimitedVdmRole() {
        return getValue(Flags.FLAG_ENABLE_LIMITED_VDM_ROLE,
            FeatureFlags::enableLimitedVdmRole);
    }

    @Override

    public boolean externalCameraDefaultPolicy() {
        return getValue(Flags.FLAG_EXTERNAL_CAMERA_DEFAULT_POLICY,
            FeatureFlags::externalCameraDefaultPolicy);
    }

    @Override

    public boolean externalVirtualCameras() {
        return getValue(Flags.FLAG_EXTERNAL_VIRTUAL_CAMERAS,
            FeatureFlags::externalVirtualCameras);
    }

    @Override

    public boolean fixVdmOptOutOnMirrorDisplays() {
        return getValue(Flags.FLAG_FIX_VDM_OPT_OUT_ON_MIRROR_DISPLAYS,
            FeatureFlags::fixVdmOptOutOnMirrorDisplays);
    }

    @Override

    public boolean handleInvalidDeviceId() {
        return getValue(Flags.FLAG_HANDLE_INVALID_DEVICE_ID,
            FeatureFlags::handleInvalidDeviceId);
    }

    @Override

    public boolean highResolutionScroll() {
        return getValue(Flags.FLAG_HIGH_RESOLUTION_SCROLL,
            FeatureFlags::highResolutionScroll);
    }

    @Override

    public boolean itemizedVdmPermissions() {
        return getValue(Flags.FLAG_ITEMIZED_VDM_PERMISSIONS,
            FeatureFlags::itemizedVdmPermissions);
    }

    @Override

    public boolean migrateViewconfigurationConstantsToResources() {
        return getValue(Flags.FLAG_MIGRATE_VIEWCONFIGURATION_CONSTANTS_TO_RESOURCES,
            FeatureFlags::migrateViewconfigurationConstantsToResources);
    }

    @Override

    public boolean notificationsForDeviceStreaming() {
        return getValue(Flags.FLAG_NOTIFICATIONS_FOR_DEVICE_STREAMING,
            FeatureFlags::notificationsForDeviceStreaming);
    }

    @Override

    public boolean statusBarAndInsets() {
        return getValue(Flags.FLAG_STATUS_BAR_AND_INSETS,
            FeatureFlags::statusBarAndInsets);
    }

    @Override

    public boolean vdmMirrorDisplayPermission() {
        return getValue(Flags.FLAG_VDM_MIRROR_DISPLAY_PERMISSION,
            FeatureFlags::vdmMirrorDisplayPermission);
    }

    @Override

    public boolean viewconfigurationApis() {
        return getValue(Flags.FLAG_VIEWCONFIGURATION_APIS,
            FeatureFlags::viewconfigurationApis);
    }

    @Override

    public boolean virtualCameraMetadata() {
        return getValue(Flags.FLAG_VIRTUAL_CAMERA_METADATA,
            FeatureFlags::virtualCameraMetadata);
    }

    @Override

    public boolean virtualCameraNoFrameDuplication() {
        return getValue(Flags.FLAG_VIRTUAL_CAMERA_NO_FRAME_DUPLICATION,
            FeatureFlags::virtualCameraNoFrameDuplication);
    }

    @Override

    public boolean virtualCameraOnOpen() {
        return getValue(Flags.FLAG_VIRTUAL_CAMERA_ON_OPEN,
            FeatureFlags::virtualCameraOnOpen);
    }

    @Override

    public boolean virtualDisplayInsets() {
        return getValue(Flags.FLAG_VIRTUAL_DISPLAY_INSETS,
            FeatureFlags::virtualDisplayInsets);
    }

    @Override

    public boolean virtualDisplayRotationApi() {
        return getValue(Flags.FLAG_VIRTUAL_DISPLAY_ROTATION_API,
            FeatureFlags::virtualDisplayRotationApi);
    }

    @Override

    public boolean virtualRotary() {
        return getValue(Flags.FLAG_VIRTUAL_ROTARY,
            FeatureFlags::virtualRotary);
    }

    @Override

    public boolean virtualSensorAdditionalInfo() {
        return getValue(Flags.FLAG_VIRTUAL_SENSOR_ADDITIONAL_INFO,
            FeatureFlags::virtualSensorAdditionalInfo);
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
            Flags.FLAG_ACTIVITY_CONTROL_API,
            Flags.FLAG_AUTOMATED_APP_LAUNCH_INTERCEPTION,
            Flags.FLAG_CAMERA_MULTIPLE_INPUT_STREAMS,
            Flags.FLAG_CAMERA_TIMESTAMP_FROM_SURFACE,
            Flags.FLAG_COMPUTER_CONTROL_ACCESS,
            Flags.FLAG_COMPUTER_CONTROL_ACTIVITY_POLICY_STRICT,
            Flags.FLAG_COMPUTER_CONTROL_CONSENT,
            Flags.FLAG_COMPUTER_CONTROL_TYPING,
            Flags.FLAG_CORRECT_VIRTUAL_DISPLAY_POWER_STATE,
            Flags.FLAG_DEFAULT_DEVICE_CAMERA_ACCESS_POLICY,
            Flags.FLAG_DEVICE_AWARE_DISPLAY_POWER,
            Flags.FLAG_DEVICE_AWARE_SETTINGS_OVERRIDE,
            Flags.FLAG_DEVICE_AWARE_UI_MODE,
            Flags.FLAG_ENABLE_ANIMATIONS_PER_DISPLAY,
            Flags.FLAG_ENABLE_LIMITED_VDM_ROLE,
            Flags.FLAG_EXTERNAL_CAMERA_DEFAULT_POLICY,
            Flags.FLAG_EXTERNAL_VIRTUAL_CAMERAS,
            Flags.FLAG_FIX_VDM_OPT_OUT_ON_MIRROR_DISPLAYS,
            Flags.FLAG_HANDLE_INVALID_DEVICE_ID,
            Flags.FLAG_HIGH_RESOLUTION_SCROLL,
            Flags.FLAG_ITEMIZED_VDM_PERMISSIONS,
            Flags.FLAG_MIGRATE_VIEWCONFIGURATION_CONSTANTS_TO_RESOURCES,
            Flags.FLAG_NOTIFICATIONS_FOR_DEVICE_STREAMING,
            Flags.FLAG_STATUS_BAR_AND_INSETS,
            Flags.FLAG_VDM_MIRROR_DISPLAY_PERMISSION,
            Flags.FLAG_VIEWCONFIGURATION_APIS,
            Flags.FLAG_VIRTUAL_CAMERA_METADATA,
            Flags.FLAG_VIRTUAL_CAMERA_NO_FRAME_DUPLICATION,
            Flags.FLAG_VIRTUAL_CAMERA_ON_OPEN,
            Flags.FLAG_VIRTUAL_DISPLAY_INSETS,
            Flags.FLAG_VIRTUAL_DISPLAY_ROTATION_API,
            Flags.FLAG_VIRTUAL_ROTARY,
            Flags.FLAG_VIRTUAL_SENSOR_ADDITIONAL_INFO
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            Flags.FLAG_MIGRATE_VIEWCONFIGURATION_CONSTANTS_TO_RESOURCES,
            ""
        )
    );
}
