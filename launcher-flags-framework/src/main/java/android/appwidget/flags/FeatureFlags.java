package android.appwidget.flags;

/** @hide */
public interface FeatureFlags {

    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean drawDataParcel();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean engagementMetrics();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean generatedPreviews();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean notKeyguardCategory();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean playStorePinWidgets();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean remoteAdapterConversion();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean remoteDocumentFeatures2025q4();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean remoteDocumentSupport();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean remoteViewsProto();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean removeAppWidgetServiceIoFromCriticalPath();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean securityPolicyInteractAcrossUsers();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean supportResumeRestoreAfterReboot();
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean throttleWidgetUpdates();
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor

    boolean useSmallerAppWidgetSystemRadius();
}
