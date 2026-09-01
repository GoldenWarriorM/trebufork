package android.os;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean adpf25q2Metrics = true;
    private static boolean adpfGpuReportActualWorkDuration = true;
    private static boolean adpfMeasureDuringInputEventBoost = false;
    private static boolean adpfPreferPowerEfficiency = true;
    private static boolean adpfUseFmqChannel = true;
    private static boolean allowConsentlessBugreportDelegatedConsent = true;
    private static boolean allowPrivateProfile = true;
    private static boolean allowThermalHalSkinForecast = true;
    private static boolean allowThermalHeadroomThresholds = true;
    private static boolean allowThermalThresholdsCallback = true;
    private static boolean androidOsBuildVanillaIceCream = true;
    private static boolean apiForBackportedFixes = true;
    private static boolean asyncTraceForTrack = true;
    private static boolean batterySaverSupportedCheckApi = true;
    private static boolean binderCacheTransactionTraceNames = true;
    private static boolean binderFrozenStateChangeCallback = true;
    private static boolean bluetoothBugreportMode = true;
    private static boolean bugreportDeferredConsentScreenshotFix = true;
    private static boolean bugreportMultiDisplayScreenshotEnabled = true;
    private static boolean countClassInstancesApi = true;
    private static boolean cpuGpuHeadrooms = true;
    private static boolean disableMadviseArtfileDefault = true;
    private static boolean enableAngleDenyList = false;
    private static boolean enableAngleForGames = false;
    private static boolean enforceStrictFileModeCheck = true;
    private static boolean getPrivateSpaceSettings = true;
    private static boolean lowLightDreamBehavior = true;
    private static boolean mainlineVcnPlatformApi = true;
    private static boolean materialColors102024 = true;
    private static boolean materialColors202503 = false;
    private static boolean materialDefaultUserIcon = false;
    private static boolean materialMotionTokens = true;
    private static boolean materialShapeTokens = true;
    private static boolean nativeFrameworkPrototype = false;
    private static boolean nativeLooperSkipEpollWaitForZeroTimeout = true;
    private static boolean networkTimeUsesSharedMemory = true;
    private static boolean orderedBroadcastMultiplePermissions = true;
    private static boolean parcelStringCacheEnabled = true;
    private static boolean perfettoSdkTracing = true;
    private static boolean perfettoSdkTracingV2 = true;
    private static boolean perfettoSdkTracingV3 = false;
    private static boolean removeAppProfilerPssCollection = true;
    private static boolean securityStateService = true;
    private static boolean stateOfHealthPublic = true;
    private static boolean strictModeRestrictedNetwork = false;
    private static boolean useConcurrentMessageQueueInApps = false;
    private static boolean useQueryAngleChoice = true;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.os", 0x5FE952A3EFEC122FL);
            batterySaverSupportedCheckApi = reader.getBooleanFlagValue(19);
            enforceStrictFileModeCheck = reader.getBooleanFlagValue(32);
            removeAppProfilerPssCollection = reader.getBooleanFlagValue(53);
            strictModeRestrictedNetwork = reader.getBooleanFlagValue(57);
            bluetoothBugreportMode = reader.getBooleanFlagValue(23);
            orderedBroadcastMultiplePermissions = reader.getBooleanFlagValue(47);
            androidOsBuildVanillaIceCream = reader.getBooleanFlagValue(15);
            allowConsentlessBugreportDelegatedConsent = reader.getBooleanFlagValue(10);
            bugreportMultiDisplayScreenshotEnabled = reader.getBooleanFlagValue(25);
            securityStateService = reader.getBooleanFlagValue(54);
            adpf25q2Metrics = reader.getBooleanFlagValue(0);
            adpfGpuReportActualWorkDuration = reader.getBooleanFlagValue(1);
            adpfMeasureDuringInputEventBoost = reader.getBooleanFlagValue(4);
            adpfPreferPowerEfficiency = reader.getBooleanFlagValue(6);
            adpfUseFmqChannel = reader.getBooleanFlagValue(7);
            allowThermalHalSkinForecast = reader.getBooleanFlagValue(12);
            allowThermalHeadroomThresholds = reader.getBooleanFlagValue(13);
            allowThermalThresholdsCallback = reader.getBooleanFlagValue(14);
            cpuGpuHeadrooms = reader.getBooleanFlagValue(27);
            enableAngleDenyList = reader.getBooleanFlagValue(29);
            enableAngleForGames = reader.getBooleanFlagValue(30);
            useQueryAngleChoice = reader.getBooleanFlagValue(61);
            apiForBackportedFixes = reader.getBooleanFlagValue(16);
            asyncTraceForTrack = reader.getBooleanFlagValue(17);
            allowPrivateProfile = reader.getBooleanFlagValue(11);
            getPrivateSpaceSettings = reader.getBooleanFlagValue(33);
            binderCacheTransactionTraceNames = reader.getBooleanFlagValue(21);
            binderFrozenStateChangeCallback = reader.getBooleanFlagValue(22);
            countClassInstancesApi = reader.getBooleanFlagValue(26);
            disableMadviseArtfileDefault = reader.getBooleanFlagValue(28);
            nativeFrameworkPrototype = reader.getBooleanFlagValue(44);
            nativeLooperSkipEpollWaitForZeroTimeout = reader.getBooleanFlagValue(45);
            networkTimeUsesSharedMemory = reader.getBooleanFlagValue(46);
            parcelStringCacheEnabled = reader.getBooleanFlagValue(49);
            perfettoSdkTracing = reader.getBooleanFlagValue(50);
            perfettoSdkTracingV2 = reader.getBooleanFlagValue(51);
            perfettoSdkTracingV3 = reader.getBooleanFlagValue(52);
            useConcurrentMessageQueueInApps = reader.getBooleanFlagValue(60);
            stateOfHealthPublic = reader.getBooleanFlagValue(55);
            lowLightDreamBehavior = reader.getBooleanFlagValue(36);
            materialColors102024 = reader.getBooleanFlagValue(38);
            materialColors202503 = reader.getBooleanFlagValue(39);
            materialDefaultUserIcon = reader.getBooleanFlagValue(40);
            materialMotionTokens = reader.getBooleanFlagValue(41);
            materialShapeTokens = reader.getBooleanFlagValue(42);
            mainlineVcnPlatformApi = reader.getBooleanFlagValue(37);
            bugreportDeferredConsentScreenshotFix = reader.getBooleanFlagValue(24);
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

    public boolean adpf25q2Metrics() {
        if (!isCached) {
            init();
        }
        return adpf25q2Metrics;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfCapMaxBatchSize() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfGpuReportActualWorkDuration() {
        if (!isCached) {
            init();
        }
        return adpfGpuReportActualWorkDuration;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfGraphicsPipeline() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfHwuiGpu() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfMeasureDuringInputEventBoost() {
        if (!isCached) {
            init();
        }
        return adpfMeasureDuringInputEventBoost;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfObtainviewBoost() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfPlatformPowerEfficiency() {
        return false;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfPreferPowerEfficiency() {
        if (!isCached) {
            init();
        }
        return adpfPreferPowerEfficiency;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfUseFmqChannel() {
        if (!isCached) {
            init();
        }
        return adpfUseFmqChannel;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfUseFmqChannelFixed() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean adpfUseLoadHints() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowConsentlessBugreportDelegatedConsent() {
        if (!isCached) {
            init();
        }
        return allowConsentlessBugreportDelegatedConsent;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowPrivateProfile() {
        if (!isCached) {
            init();
        }
        return allowPrivateProfile;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowThermalHalSkinForecast() {
        if (!isCached) {
            init();
        }
        return allowThermalHalSkinForecast;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowThermalHeadroomThresholds() {
        if (!isCached) {
            init();
        }
        return allowThermalHeadroomThresholds;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean allowThermalThresholdsCallback() {
        if (!isCached) {
            init();
        }
        return allowThermalThresholdsCallback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean androidOsBuildVanillaIceCream() {
        if (!isCached) {
            init();
        }
        return androidOsBuildVanillaIceCream;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean apiForBackportedFixes() {
        if (!isCached) {
            init();
        }
        return apiForBackportedFixes;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean asyncTraceForTrack() {
        if (!isCached) {
            init();
        }
        return asyncTraceForTrack;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean batteryPartStatusApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean batterySaverSupportedCheckApi() {
        if (!isCached) {
            init();
        }
        return batterySaverSupportedCheckApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean batteryServiceSupportCurrentAdbCommand() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean binderCacheTransactionTraceNames() {
        if (!isCached) {
            init();
        }
        return binderCacheTransactionTraceNames;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean binderFrozenStateChangeCallback() {
        if (!isCached) {
            init();
        }
        return binderFrozenStateChangeCallback;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bluetoothBugreportMode() {
        if (!isCached) {
            init();
        }
        return bluetoothBugreportMode;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bugreportDeferredConsentScreenshotFix() {
        if (!isCached) {
            init();
        }
        return bugreportDeferredConsentScreenshotFix;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean bugreportMultiDisplayScreenshotEnabled() {
        if (!isCached) {
            init();
        }
        return bugreportMultiDisplayScreenshotEnabled;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean countClassInstancesApi() {
        if (!isCached) {
            init();
        }
        return countClassInstancesApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean cpuGpuHeadrooms() {
        if (!isCached) {
            init();
        }
        return cpuGpuHeadrooms;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean disableMadviseArtfileDefault() {
        if (!isCached) {
            init();
        }
        return disableMadviseArtfileDefault;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAngleDenyList() {
        if (!isCached) {
            init();
        }
        return enableAngleDenyList;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableAngleForGames() {
        if (!isCached) {
            init();
        }
        return enableAngleForGames;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enableHasBinders() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean enforceStrictFileModeCheck() {
        if (!isCached) {
            init();
        }
        return enforceStrictFileModeCheck;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean getPrivateSpaceSettings() {
        if (!isCached) {
            init();
        }
        return getPrivateSpaceSettings;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ipcDataCacheModuleAdservices() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean ipcDataCacheTestmodeApis() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean lowLightDreamBehavior() {
        if (!isCached) {
            init();
        }
        return lowLightDreamBehavior;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean mainlineVcnPlatformApi() {
        if (!isCached) {
            init();
        }
        return mainlineVcnPlatformApi;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean materialColors102024() {
        if (!isCached) {
            init();
        }
        return materialColors102024;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean materialColors202503() {
        if (!isCached) {
            init();
        }
        return materialColors202503;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean materialDefaultUserIcon() {
        if (!isCached) {
            init();
        }
        return materialDefaultUserIcon;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean materialMotionTokens() {
        if (!isCached) {
            init();
        }
        return materialMotionTokens;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean materialShapeTokens() {
        if (!isCached) {
            init();
        }
        return materialShapeTokens;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean messageQueueTestability() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean nativeFrameworkPrototype() {
        if (!isCached) {
            init();
        }
        return nativeFrameworkPrototype;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean nativeLooperSkipEpollWaitForZeroTimeout() {
        if (!isCached) {
            init();
        }
        return nativeLooperSkipEpollWaitForZeroTimeout;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean networkTimeUsesSharedMemory() {
        if (!isCached) {
            init();
        }
        return networkTimeUsesSharedMemory;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean orderedBroadcastMultiplePermissions() {
        if (!isCached) {
            init();
        }
        return orderedBroadcastMultiplePermissions;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean parcelMarshallBytebuffer() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean parcelStringCacheEnabled() {
        if (!isCached) {
            init();
        }
        return parcelStringCacheEnabled;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean perfettoSdkTracing() {
        if (!isCached) {
            init();
        }
        return perfettoSdkTracing;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean perfettoSdkTracingV2() {
        if (!isCached) {
            init();
        }
        return perfettoSdkTracingV2;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean perfettoSdkTracingV3() {
        if (!isCached) {
            init();
        }
        return perfettoSdkTracingV3;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeAppProfilerPssCollection() {
        if (!isCached) {
            init();
        }
        return removeAppProfilerPssCollection;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean securityStateService() {
        if (!isCached) {
            init();
        }
        return securityStateService;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean stateOfHealthPublic() {
        if (!isCached) {
            init();
        }
        return stateOfHealthPublic;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean storageLifetimeApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean strictModeRestrictedNetwork() {
        if (!isCached) {
            init();
        }
        return strictModeRestrictedNetwork;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean telemetryApisFrameworkInitialization() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean updateEngineApi() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useConcurrentMessageQueueInApps() {
        if (!isCached) {
            init();
        }
        return useConcurrentMessageQueueInApps;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useQueryAngleChoice() {
        if (!isCached) {
            init();
        }
        return useQueryAngleChoice;
    }

}
