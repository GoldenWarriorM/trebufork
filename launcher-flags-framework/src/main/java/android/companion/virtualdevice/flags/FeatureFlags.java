package android.companion.virtualdevice.flags;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean activityControlApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean automatedAppLaunchInterception();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cameraMultipleInputStreams();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cameraTimestampFromSurface();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean computerControlAccess();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean computerControlActivityPolicyStrict();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean computerControlConsent();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean computerControlTyping();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean correctVirtualDisplayPowerState();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean defaultDeviceCameraAccessPolicy();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceAwareDisplayPower();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceAwareSettingsOverride();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean deviceAwareUiMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAnimationsPerDisplay();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableLimitedVdmRole();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean externalCameraDefaultPolicy();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean externalVirtualCameras();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean fixVdmOptOutOnMirrorDisplays();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean handleInvalidDeviceId();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean highResolutionScroll();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean itemizedVdmPermissions();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean migrateViewconfigurationConstantsToResources();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notificationsForDeviceStreaming();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean statusBarAndInsets();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean vdmMirrorDisplayPermission();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean viewconfigurationApis();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualCameraMetadata();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualCameraNoFrameDuplication();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualCameraOnOpen();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualDisplayInsets();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualDisplayRotationApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualRotary();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean virtualSensorAdditionalInfo();
}
