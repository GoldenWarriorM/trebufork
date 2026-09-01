package android.appwidget.flags;


/** @hide */
public final class Flags {
    /** @hide */
    public static final String FLAG_DRAW_DATA_PARCEL = "android.appwidget.flags.draw_data_parcel";
    /** @hide */
    public static final String FLAG_ENGAGEMENT_METRICS = "android.appwidget.flags.engagement_metrics";
    /** @hide */
    public static final String FLAG_GENERATED_PREVIEWS = "android.appwidget.flags.generated_previews";
    /** @hide */
    public static final String FLAG_NOT_KEYGUARD_CATEGORY = "android.appwidget.flags.not_keyguard_category";
    /** @hide */
    public static final String FLAG_PLAY_STORE_PIN_WIDGETS = "android.appwidget.flags.play_store_pin_widgets";
    /** @hide */
    public static final String FLAG_REMOTE_ADAPTER_CONVERSION = "android.appwidget.flags.remote_adapter_conversion";
    /** @hide */
    public static final String FLAG_REMOTE_DOCUMENT_FEATURES_2025Q4 = "android.appwidget.flags.remote_document_features_2025q4";
    /** @hide */
    public static final String FLAG_REMOTE_DOCUMENT_SUPPORT = "android.appwidget.flags.remote_document_support";
    /** @hide */
    public static final String FLAG_REMOTE_VIEWS_PROTO = "android.appwidget.flags.remote_views_proto";
    /** @hide */
    public static final String FLAG_REMOVE_APP_WIDGET_SERVICE_IO_FROM_CRITICAL_PATH = "android.appwidget.flags.remove_app_widget_service_io_from_critical_path";
    /** @hide */
    public static final String FLAG_SECURITY_POLICY_INTERACT_ACROSS_USERS = "android.appwidget.flags.security_policy_interact_across_users";
    /** @hide */
    public static final String FLAG_SUPPORT_RESUME_RESTORE_AFTER_REBOOT = "android.appwidget.flags.support_resume_restore_after_reboot";
    /** @hide */
    public static final String FLAG_THROTTLE_WIDGET_UPDATES = "android.appwidget.flags.throttle_widget_updates";
    /** @hide */
    public static final String FLAG_USE_SMALLER_APP_WIDGET_SYSTEM_RADIUS = "android.appwidget.flags.use_smaller_app_widget_system_radius";
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean drawDataParcel() {
        
        return FEATURE_FLAGS.drawDataParcel();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean engagementMetrics() {
        
        return FEATURE_FLAGS.engagementMetrics();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean generatedPreviews() {
        
        return FEATURE_FLAGS.generatedPreviews();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean notKeyguardCategory() {
        
        return FEATURE_FLAGS.notKeyguardCategory();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean playStorePinWidgets() {
        
        return FEATURE_FLAGS.playStorePinWidgets();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean remoteAdapterConversion() {
        
        return FEATURE_FLAGS.remoteAdapterConversion();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean remoteDocumentFeatures2025q4() {
        
        return FEATURE_FLAGS.remoteDocumentFeatures2025q4();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean remoteDocumentSupport() {
        
        return FEATURE_FLAGS.remoteDocumentSupport();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean remoteViewsProto() {
        
        return FEATURE_FLAGS.remoteViewsProto();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean removeAppWidgetServiceIoFromCriticalPath() {
        
        return FEATURE_FLAGS.removeAppWidgetServiceIoFromCriticalPath();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean securityPolicyInteractAcrossUsers() {
        
        return FEATURE_FLAGS.securityPolicyInteractAcrossUsers();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean supportResumeRestoreAfterReboot() {
        
        return FEATURE_FLAGS.supportResumeRestoreAfterReboot();
    }
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean throttleWidgetUpdates() {
        
        return FEATURE_FLAGS.throttleWidgetUpdates();
    }
    @com.android.aconfig.annotations.AssumeTrueForR8
    @com.android.aconfig.annotations.AconfigFlagAccessor
    public static boolean useSmallerAppWidgetSystemRadius() {
        
        return FEATURE_FLAGS.useSmallerAppWidgetSystemRadius();
    }

    private static FeatureFlags FEATURE_FLAGS = new FeatureFlagsImpl();

}
