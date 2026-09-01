package android.os;


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

    public boolean adpf25q2Metrics() {
        return getValue(Flags.FLAG_ADPF_25Q2_METRICS,
            FeatureFlags::adpf25q2Metrics);
    }

    @Override

    public boolean adpfCapMaxBatchSize() {
        return getValue(Flags.FLAG_ADPF_CAP_MAX_BATCH_SIZE,
            FeatureFlags::adpfCapMaxBatchSize);
    }

    @Override

    public boolean adpfGpuReportActualWorkDuration() {
        return getValue(Flags.FLAG_ADPF_GPU_REPORT_ACTUAL_WORK_DURATION,
            FeatureFlags::adpfGpuReportActualWorkDuration);
    }

    @Override

    public boolean adpfGraphicsPipeline() {
        return getValue(Flags.FLAG_ADPF_GRAPHICS_PIPELINE,
            FeatureFlags::adpfGraphicsPipeline);
    }

    @Override

    public boolean adpfHwuiGpu() {
        return getValue(Flags.FLAG_ADPF_HWUI_GPU,
            FeatureFlags::adpfHwuiGpu);
    }

    @Override

    public boolean adpfMeasureDuringInputEventBoost() {
        return getValue(Flags.FLAG_ADPF_MEASURE_DURING_INPUT_EVENT_BOOST,
            FeatureFlags::adpfMeasureDuringInputEventBoost);
    }

    @Override

    public boolean adpfObtainviewBoost() {
        return getValue(Flags.FLAG_ADPF_OBTAINVIEW_BOOST,
            FeatureFlags::adpfObtainviewBoost);
    }

    @Override

    public boolean adpfPlatformPowerEfficiency() {
        return getValue(Flags.FLAG_ADPF_PLATFORM_POWER_EFFICIENCY,
            FeatureFlags::adpfPlatformPowerEfficiency);
    }

    @Override

    public boolean adpfPreferPowerEfficiency() {
        return getValue(Flags.FLAG_ADPF_PREFER_POWER_EFFICIENCY,
            FeatureFlags::adpfPreferPowerEfficiency);
    }

    @Override

    public boolean adpfUseFmqChannel() {
        return getValue(Flags.FLAG_ADPF_USE_FMQ_CHANNEL,
            FeatureFlags::adpfUseFmqChannel);
    }

    @Override

    public boolean adpfUseFmqChannelFixed() {
        return getValue(Flags.FLAG_ADPF_USE_FMQ_CHANNEL_FIXED,
            FeatureFlags::adpfUseFmqChannelFixed);
    }

    @Override

    public boolean adpfUseLoadHints() {
        return getValue(Flags.FLAG_ADPF_USE_LOAD_HINTS,
            FeatureFlags::adpfUseLoadHints);
    }

    @Override

    public boolean allowConsentlessBugreportDelegatedConsent() {
        return getValue(Flags.FLAG_ALLOW_CONSENTLESS_BUGREPORT_DELEGATED_CONSENT,
            FeatureFlags::allowConsentlessBugreportDelegatedConsent);
    }

    @Override

    public boolean allowPrivateProfile() {
        return getValue(Flags.FLAG_ALLOW_PRIVATE_PROFILE,
            FeatureFlags::allowPrivateProfile);
    }

    @Override

    public boolean allowThermalHalSkinForecast() {
        return getValue(Flags.FLAG_ALLOW_THERMAL_HAL_SKIN_FORECAST,
            FeatureFlags::allowThermalHalSkinForecast);
    }

    @Override

    public boolean allowThermalHeadroomThresholds() {
        return getValue(Flags.FLAG_ALLOW_THERMAL_HEADROOM_THRESHOLDS,
            FeatureFlags::allowThermalHeadroomThresholds);
    }

    @Override

    public boolean allowThermalThresholdsCallback() {
        return getValue(Flags.FLAG_ALLOW_THERMAL_THRESHOLDS_CALLBACK,
            FeatureFlags::allowThermalThresholdsCallback);
    }

    @Override

    public boolean androidOsBuildVanillaIceCream() {
        return getValue(Flags.FLAG_ANDROID_OS_BUILD_VANILLA_ICE_CREAM,
            FeatureFlags::androidOsBuildVanillaIceCream);
    }

    @Override

    public boolean apiForBackportedFixes() {
        return getValue(Flags.FLAG_API_FOR_BACKPORTED_FIXES,
            FeatureFlags::apiForBackportedFixes);
    }

    @Override

    public boolean asyncTraceForTrack() {
        return getValue(Flags.FLAG_ASYNC_TRACE_FOR_TRACK,
            FeatureFlags::asyncTraceForTrack);
    }

    @Override

    public boolean batteryPartStatusApi() {
        return getValue(Flags.FLAG_BATTERY_PART_STATUS_API,
            FeatureFlags::batteryPartStatusApi);
    }

    @Override

    public boolean batterySaverSupportedCheckApi() {
        return getValue(Flags.FLAG_BATTERY_SAVER_SUPPORTED_CHECK_API,
            FeatureFlags::batterySaverSupportedCheckApi);
    }

    @Override

    public boolean batteryServiceSupportCurrentAdbCommand() {
        return getValue(Flags.FLAG_BATTERY_SERVICE_SUPPORT_CURRENT_ADB_COMMAND,
            FeatureFlags::batteryServiceSupportCurrentAdbCommand);
    }

    @Override

    public boolean binderCacheTransactionTraceNames() {
        return getValue(Flags.FLAG_BINDER_CACHE_TRANSACTION_TRACE_NAMES,
            FeatureFlags::binderCacheTransactionTraceNames);
    }

    @Override

    public boolean binderFrozenStateChangeCallback() {
        return getValue(Flags.FLAG_BINDER_FROZEN_STATE_CHANGE_CALLBACK,
            FeatureFlags::binderFrozenStateChangeCallback);
    }

    @Override

    public boolean bluetoothBugreportMode() {
        return getValue(Flags.FLAG_BLUETOOTH_BUGREPORT_MODE,
            FeatureFlags::bluetoothBugreportMode);
    }

    @Override

    public boolean bugreportDeferredConsentScreenshotFix() {
        return getValue(Flags.FLAG_BUGREPORT_DEFERRED_CONSENT_SCREENSHOT_FIX,
            FeatureFlags::bugreportDeferredConsentScreenshotFix);
    }

    @Override

    public boolean bugreportMultiDisplayScreenshotEnabled() {
        return getValue(Flags.FLAG_BUGREPORT_MULTI_DISPLAY_SCREENSHOT_ENABLED,
            FeatureFlags::bugreportMultiDisplayScreenshotEnabled);
    }

    @Override

    public boolean countClassInstancesApi() {
        return getValue(Flags.FLAG_COUNT_CLASS_INSTANCES_API,
            FeatureFlags::countClassInstancesApi);
    }

    @Override

    public boolean cpuGpuHeadrooms() {
        return getValue(Flags.FLAG_CPU_GPU_HEADROOMS,
            FeatureFlags::cpuGpuHeadrooms);
    }

    @Override

    public boolean disableMadviseArtfileDefault() {
        return getValue(Flags.FLAG_DISABLE_MADVISE_ARTFILE_DEFAULT,
            FeatureFlags::disableMadviseArtfileDefault);
    }

    @Override

    public boolean enableAngleDenyList() {
        return getValue(Flags.FLAG_ENABLE_ANGLE_DENY_LIST,
            FeatureFlags::enableAngleDenyList);
    }

    @Override

    public boolean enableAngleForGames() {
        return getValue(Flags.FLAG_ENABLE_ANGLE_FOR_GAMES,
            FeatureFlags::enableAngleForGames);
    }

    @Override

    public boolean enableHasBinders() {
        return getValue(Flags.FLAG_ENABLE_HAS_BINDERS,
            FeatureFlags::enableHasBinders);
    }

    @Override

    public boolean enforceStrictFileModeCheck() {
        return getValue(Flags.FLAG_ENFORCE_STRICT_FILE_MODE_CHECK,
            FeatureFlags::enforceStrictFileModeCheck);
    }

    @Override

    public boolean getPrivateSpaceSettings() {
        return getValue(Flags.FLAG_GET_PRIVATE_SPACE_SETTINGS,
            FeatureFlags::getPrivateSpaceSettings);
    }

    @Override

    public boolean ipcDataCacheModuleAdservices() {
        return getValue(Flags.FLAG_IPC_DATA_CACHE_MODULE_ADSERVICES,
            FeatureFlags::ipcDataCacheModuleAdservices);
    }

    @Override

    public boolean ipcDataCacheTestmodeApis() {
        return getValue(Flags.FLAG_IPC_DATA_CACHE_TESTMODE_APIS,
            FeatureFlags::ipcDataCacheTestmodeApis);
    }

    @Override

    public boolean lowLightDreamBehavior() {
        return getValue(Flags.FLAG_LOW_LIGHT_DREAM_BEHAVIOR,
            FeatureFlags::lowLightDreamBehavior);
    }

    @Override

    public boolean mainlineVcnPlatformApi() {
        return getValue(Flags.FLAG_MAINLINE_VCN_PLATFORM_API,
            FeatureFlags::mainlineVcnPlatformApi);
    }

    @Override

    public boolean materialColors102024() {
        return getValue(Flags.FLAG_MATERIAL_COLORS_10_2024,
            FeatureFlags::materialColors102024);
    }

    @Override

    public boolean materialColors202503() {
        return getValue(Flags.FLAG_MATERIAL_COLORS_2025_03,
            FeatureFlags::materialColors202503);
    }

    @Override

    public boolean materialDefaultUserIcon() {
        return getValue(Flags.FLAG_MATERIAL_DEFAULT_USER_ICON,
            FeatureFlags::materialDefaultUserIcon);
    }

    @Override

    public boolean materialMotionTokens() {
        return getValue(Flags.FLAG_MATERIAL_MOTION_TOKENS,
            FeatureFlags::materialMotionTokens);
    }

    @Override

    public boolean materialShapeTokens() {
        return getValue(Flags.FLAG_MATERIAL_SHAPE_TOKENS,
            FeatureFlags::materialShapeTokens);
    }

    @Override

    public boolean messageQueueTestability() {
        return getValue(Flags.FLAG_MESSAGE_QUEUE_TESTABILITY,
            FeatureFlags::messageQueueTestability);
    }

    @Override

    public boolean nativeFrameworkPrototype() {
        return getValue(Flags.FLAG_NATIVE_FRAMEWORK_PROTOTYPE,
            FeatureFlags::nativeFrameworkPrototype);
    }

    @Override

    public boolean nativeLooperSkipEpollWaitForZeroTimeout() {
        return getValue(Flags.FLAG_NATIVE_LOOPER_SKIP_EPOLL_WAIT_FOR_ZERO_TIMEOUT,
            FeatureFlags::nativeLooperSkipEpollWaitForZeroTimeout);
    }

    @Override

    public boolean networkTimeUsesSharedMemory() {
        return getValue(Flags.FLAG_NETWORK_TIME_USES_SHARED_MEMORY,
            FeatureFlags::networkTimeUsesSharedMemory);
    }

    @Override

    public boolean orderedBroadcastMultiplePermissions() {
        return getValue(Flags.FLAG_ORDERED_BROADCAST_MULTIPLE_PERMISSIONS,
            FeatureFlags::orderedBroadcastMultiplePermissions);
    }

    @Override

    public boolean parcelMarshallBytebuffer() {
        return getValue(Flags.FLAG_PARCEL_MARSHALL_BYTEBUFFER,
            FeatureFlags::parcelMarshallBytebuffer);
    }

    @Override

    public boolean parcelStringCacheEnabled() {
        return getValue(Flags.FLAG_PARCEL_STRING_CACHE_ENABLED,
            FeatureFlags::parcelStringCacheEnabled);
    }

    @Override

    public boolean perfettoSdkTracing() {
        return getValue(Flags.FLAG_PERFETTO_SDK_TRACING,
            FeatureFlags::perfettoSdkTracing);
    }

    @Override

    public boolean perfettoSdkTracingV2() {
        return getValue(Flags.FLAG_PERFETTO_SDK_TRACING_V2,
            FeatureFlags::perfettoSdkTracingV2);
    }

    @Override

    public boolean perfettoSdkTracingV3() {
        return getValue(Flags.FLAG_PERFETTO_SDK_TRACING_V3,
            FeatureFlags::perfettoSdkTracingV3);
    }

    @Override

    public boolean removeAppProfilerPssCollection() {
        return getValue(Flags.FLAG_REMOVE_APP_PROFILER_PSS_COLLECTION,
            FeatureFlags::removeAppProfilerPssCollection);
    }

    @Override

    public boolean securityStateService() {
        return getValue(Flags.FLAG_SECURITY_STATE_SERVICE,
            FeatureFlags::securityStateService);
    }

    @Override

    public boolean stateOfHealthPublic() {
        return getValue(Flags.FLAG_STATE_OF_HEALTH_PUBLIC,
            FeatureFlags::stateOfHealthPublic);
    }

    @Override

    public boolean storageLifetimeApi() {
        return getValue(Flags.FLAG_STORAGE_LIFETIME_API,
            FeatureFlags::storageLifetimeApi);
    }

    @Override

    public boolean strictModeRestrictedNetwork() {
        return getValue(Flags.FLAG_STRICT_MODE_RESTRICTED_NETWORK,
            FeatureFlags::strictModeRestrictedNetwork);
    }

    @Override

    public boolean telemetryApisFrameworkInitialization() {
        return getValue(Flags.FLAG_TELEMETRY_APIS_FRAMEWORK_INITIALIZATION,
            FeatureFlags::telemetryApisFrameworkInitialization);
    }

    @Override

    public boolean updateEngineApi() {
        return getValue(Flags.FLAG_UPDATE_ENGINE_API,
            FeatureFlags::updateEngineApi);
    }

    @Override

    public boolean useConcurrentMessageQueueInApps() {
        return getValue(Flags.FLAG_USE_CONCURRENT_MESSAGE_QUEUE_IN_APPS,
            FeatureFlags::useConcurrentMessageQueueInApps);
    }

    @Override

    public boolean useQueryAngleChoice() {
        return getValue(Flags.FLAG_USE_QUERY_ANGLE_CHOICE,
            FeatureFlags::useQueryAngleChoice);
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
            Flags.FLAG_ADPF_25Q2_METRICS,
            Flags.FLAG_ADPF_CAP_MAX_BATCH_SIZE,
            Flags.FLAG_ADPF_GPU_REPORT_ACTUAL_WORK_DURATION,
            Flags.FLAG_ADPF_GRAPHICS_PIPELINE,
            Flags.FLAG_ADPF_HWUI_GPU,
            Flags.FLAG_ADPF_MEASURE_DURING_INPUT_EVENT_BOOST,
            Flags.FLAG_ADPF_OBTAINVIEW_BOOST,
            Flags.FLAG_ADPF_PLATFORM_POWER_EFFICIENCY,
            Flags.FLAG_ADPF_PREFER_POWER_EFFICIENCY,
            Flags.FLAG_ADPF_USE_FMQ_CHANNEL,
            Flags.FLAG_ADPF_USE_FMQ_CHANNEL_FIXED,
            Flags.FLAG_ADPF_USE_LOAD_HINTS,
            Flags.FLAG_ALLOW_CONSENTLESS_BUGREPORT_DELEGATED_CONSENT,
            Flags.FLAG_ALLOW_PRIVATE_PROFILE,
            Flags.FLAG_ALLOW_THERMAL_HAL_SKIN_FORECAST,
            Flags.FLAG_ALLOW_THERMAL_HEADROOM_THRESHOLDS,
            Flags.FLAG_ALLOW_THERMAL_THRESHOLDS_CALLBACK,
            Flags.FLAG_ANDROID_OS_BUILD_VANILLA_ICE_CREAM,
            Flags.FLAG_API_FOR_BACKPORTED_FIXES,
            Flags.FLAG_ASYNC_TRACE_FOR_TRACK,
            Flags.FLAG_BATTERY_PART_STATUS_API,
            Flags.FLAG_BATTERY_SAVER_SUPPORTED_CHECK_API,
            Flags.FLAG_BATTERY_SERVICE_SUPPORT_CURRENT_ADB_COMMAND,
            Flags.FLAG_BINDER_CACHE_TRANSACTION_TRACE_NAMES,
            Flags.FLAG_BINDER_FROZEN_STATE_CHANGE_CALLBACK,
            Flags.FLAG_BLUETOOTH_BUGREPORT_MODE,
            Flags.FLAG_BUGREPORT_DEFERRED_CONSENT_SCREENSHOT_FIX,
            Flags.FLAG_BUGREPORT_MULTI_DISPLAY_SCREENSHOT_ENABLED,
            Flags.FLAG_COUNT_CLASS_INSTANCES_API,
            Flags.FLAG_CPU_GPU_HEADROOMS,
            Flags.FLAG_DISABLE_MADVISE_ARTFILE_DEFAULT,
            Flags.FLAG_ENABLE_ANGLE_DENY_LIST,
            Flags.FLAG_ENABLE_ANGLE_FOR_GAMES,
            Flags.FLAG_ENABLE_HAS_BINDERS,
            Flags.FLAG_ENFORCE_STRICT_FILE_MODE_CHECK,
            Flags.FLAG_GET_PRIVATE_SPACE_SETTINGS,
            Flags.FLAG_IPC_DATA_CACHE_MODULE_ADSERVICES,
            Flags.FLAG_IPC_DATA_CACHE_TESTMODE_APIS,
            Flags.FLAG_LOW_LIGHT_DREAM_BEHAVIOR,
            Flags.FLAG_MAINLINE_VCN_PLATFORM_API,
            Flags.FLAG_MATERIAL_COLORS_10_2024,
            Flags.FLAG_MATERIAL_COLORS_2025_03,
            Flags.FLAG_MATERIAL_DEFAULT_USER_ICON,
            Flags.FLAG_MATERIAL_MOTION_TOKENS,
            Flags.FLAG_MATERIAL_SHAPE_TOKENS,
            Flags.FLAG_MESSAGE_QUEUE_TESTABILITY,
            Flags.FLAG_NATIVE_FRAMEWORK_PROTOTYPE,
            Flags.FLAG_NATIVE_LOOPER_SKIP_EPOLL_WAIT_FOR_ZERO_TIMEOUT,
            Flags.FLAG_NETWORK_TIME_USES_SHARED_MEMORY,
            Flags.FLAG_ORDERED_BROADCAST_MULTIPLE_PERMISSIONS,
            Flags.FLAG_PARCEL_MARSHALL_BYTEBUFFER,
            Flags.FLAG_PARCEL_STRING_CACHE_ENABLED,
            Flags.FLAG_PERFETTO_SDK_TRACING,
            Flags.FLAG_PERFETTO_SDK_TRACING_V2,
            Flags.FLAG_PERFETTO_SDK_TRACING_V3,
            Flags.FLAG_REMOVE_APP_PROFILER_PSS_COLLECTION,
            Flags.FLAG_SECURITY_STATE_SERVICE,
            Flags.FLAG_STATE_OF_HEALTH_PUBLIC,
            Flags.FLAG_STORAGE_LIFETIME_API,
            Flags.FLAG_STRICT_MODE_RESTRICTED_NETWORK,
            Flags.FLAG_TELEMETRY_APIS_FRAMEWORK_INITIALIZATION,
            Flags.FLAG_UPDATE_ENGINE_API,
            Flags.FLAG_USE_CONCURRENT_MESSAGE_QUEUE_IN_APPS,
            Flags.FLAG_USE_QUERY_ANGLE_CHOICE
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            Flags.FLAG_ADPF_CAP_MAX_BATCH_SIZE,
            Flags.FLAG_ADPF_GRAPHICS_PIPELINE,
            Flags.FLAG_ADPF_HWUI_GPU,
            Flags.FLAG_ADPF_OBTAINVIEW_BOOST,
            Flags.FLAG_ADPF_PLATFORM_POWER_EFFICIENCY,
            Flags.FLAG_ADPF_USE_FMQ_CHANNEL_FIXED,
            Flags.FLAG_ADPF_USE_LOAD_HINTS,
            Flags.FLAG_BATTERY_PART_STATUS_API,
            Flags.FLAG_BATTERY_SERVICE_SUPPORT_CURRENT_ADB_COMMAND,
            Flags.FLAG_ENABLE_HAS_BINDERS,
            Flags.FLAG_IPC_DATA_CACHE_MODULE_ADSERVICES,
            Flags.FLAG_IPC_DATA_CACHE_TESTMODE_APIS,
            Flags.FLAG_MESSAGE_QUEUE_TESTABILITY,
            Flags.FLAG_PARCEL_MARSHALL_BYTEBUFFER,
            Flags.FLAG_STORAGE_LIFETIME_API,
            Flags.FLAG_TELEMETRY_APIS_FRAMEWORK_INITIALIZATION,
            Flags.FLAG_UPDATE_ENGINE_API,
            ""
        )
    );
}
