package android.os;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpf25q2Metrics();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfCapMaxBatchSize();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfGpuReportActualWorkDuration();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfGraphicsPipeline();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfHwuiGpu();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfMeasureDuringInputEventBoost();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfObtainviewBoost();
    @com.android.aconfig.annotations.AssumeFalseForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfPlatformPowerEfficiency();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfPreferPowerEfficiency();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfUseFmqChannel();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfUseFmqChannelFixed();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean adpfUseLoadHints();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowConsentlessBugreportDelegatedConsent();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowPrivateProfile();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowThermalHalSkinForecast();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowThermalHeadroomThresholds();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean allowThermalThresholdsCallback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean androidOsBuildVanillaIceCream();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean apiForBackportedFixes();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean asyncTraceForTrack();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean batteryPartStatusApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean batterySaverSupportedCheckApi();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean batteryServiceSupportCurrentAdbCommand();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean binderCacheTransactionTraceNames();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean binderFrozenStateChangeCallback();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bluetoothBugreportMode();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bugreportDeferredConsentScreenshotFix();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean bugreportMultiDisplayScreenshotEnabled();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean countClassInstancesApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean cpuGpuHeadrooms();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean disableMadviseArtfileDefault();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAngleDenyList();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableAngleForGames();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enableHasBinders();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean enforceStrictFileModeCheck();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean getPrivateSpaceSettings();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ipcDataCacheModuleAdservices();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean ipcDataCacheTestmodeApis();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean lowLightDreamBehavior();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean mainlineVcnPlatformApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean materialColors102024();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean materialColors202503();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean materialDefaultUserIcon();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean materialMotionTokens();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean materialShapeTokens();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean messageQueueTestability();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean nativeFrameworkPrototype();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean nativeLooperSkipEpollWaitForZeroTimeout();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean networkTimeUsesSharedMemory();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean orderedBroadcastMultiplePermissions();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean parcelMarshallBytebuffer();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean parcelStringCacheEnabled();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean perfettoSdkTracing();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean perfettoSdkTracingV2();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean perfettoSdkTracingV3();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeAppProfilerPssCollection();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean securityStateService();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean stateOfHealthPublic();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean storageLifetimeApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean strictModeRestrictedNetwork();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean telemetryApisFrameworkInitialization();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean updateEngineApi();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useConcurrentMessageQueueInApps();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useQueryAngleChoice();
}
