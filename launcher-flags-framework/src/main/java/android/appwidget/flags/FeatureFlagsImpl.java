package android.appwidget.flags;


import android.os.flagging.PlatformAconfigPackageInternal;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImpl";
    private static volatile boolean isCached = false;

    private static boolean drawDataParcel = true;
    private static boolean engagementMetrics = true;
    private static boolean generatedPreviews = true;
    private static boolean playStorePinWidgets = true;
    private static boolean remoteAdapterConversion = true;
    private static boolean remoteDocumentFeatures2025q4 = true;
    private static boolean remoteDocumentSupport = true;
    private static boolean remoteViewsProto = true;
    private static boolean removeAppWidgetServiceIoFromCriticalPath = true;
    private static boolean securityPolicyInteractAcrossUsers = true;
    private static boolean supportResumeRestoreAfterReboot = false;
    private static boolean throttleWidgetUpdates = false;

    private void init() {
        try {

            PlatformAconfigPackageInternal reader = PlatformAconfigPackageInternal.load("android.appwidget.flags", 0x21BD579AF1950134L);
            drawDataParcel = reader.getBooleanFlagValue(0);
            engagementMetrics = reader.getBooleanFlagValue(1);
            generatedPreviews = reader.getBooleanFlagValue(2);
            playStorePinWidgets = reader.getBooleanFlagValue(4);
            remoteAdapterConversion = reader.getBooleanFlagValue(5);
            remoteDocumentFeatures2025q4 = reader.getBooleanFlagValue(6);
            remoteDocumentSupport = reader.getBooleanFlagValue(7);
            remoteViewsProto = reader.getBooleanFlagValue(8);
            removeAppWidgetServiceIoFromCriticalPath = reader.getBooleanFlagValue(9);
            securityPolicyInteractAcrossUsers = reader.getBooleanFlagValue(10);
            supportResumeRestoreAfterReboot = reader.getBooleanFlagValue(11);
            throttleWidgetUpdates = reader.getBooleanFlagValue(12);
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

    public boolean drawDataParcel() {
        if (!isCached) {
            init();
        }
        return drawDataParcel;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean engagementMetrics() {
        if (!isCached) {
            init();
        }
        return engagementMetrics;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean generatedPreviews() {
        if (!isCached) {
            init();
        }
        return generatedPreviews;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean notKeyguardCategory() {
        return true;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean playStorePinWidgets() {
        if (!isCached) {
            init();
        }
        return playStorePinWidgets;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean remoteAdapterConversion() {
        if (!isCached) {
            init();
        }
        return remoteAdapterConversion;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean remoteDocumentFeatures2025q4() {
        if (!isCached) {
            init();
        }
        return remoteDocumentFeatures2025q4;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean remoteDocumentSupport() {
        if (!isCached) {
            init();
        }
        return remoteDocumentSupport;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean remoteViewsProto() {
        if (!isCached) {
            init();
        }
        return remoteViewsProto;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean removeAppWidgetServiceIoFromCriticalPath() {
        if (!isCached) {
            init();
        }
        return removeAppWidgetServiceIoFromCriticalPath;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean securityPolicyInteractAcrossUsers() {
        if (!isCached) {
            init();
        }
        return securityPolicyInteractAcrossUsers;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean supportResumeRestoreAfterReboot() {
        if (!isCached) {
            init();
        }
        return supportResumeRestoreAfterReboot;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean throttleWidgetUpdates() {
        if (!isCached) {
            init();
        }
        return throttleWidgetUpdates;
    }

    @Override
    @com.android.aconfig.annotations.AconfigFlagAccessor

    public boolean useSmallerAppWidgetSystemRadius() {
        return true;
    }

}
