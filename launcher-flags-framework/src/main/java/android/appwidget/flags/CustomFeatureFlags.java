package android.appwidget.flags;


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

    public boolean drawDataParcel() {
        return getValue(Flags.FLAG_DRAW_DATA_PARCEL,
            FeatureFlags::drawDataParcel);
    }

    @Override

    public boolean engagementMetrics() {
        return getValue(Flags.FLAG_ENGAGEMENT_METRICS,
            FeatureFlags::engagementMetrics);
    }

    @Override

    public boolean generatedPreviews() {
        return getValue(Flags.FLAG_GENERATED_PREVIEWS,
            FeatureFlags::generatedPreviews);
    }

    @Override

    public boolean notKeyguardCategory() {
        return getValue(Flags.FLAG_NOT_KEYGUARD_CATEGORY,
            FeatureFlags::notKeyguardCategory);
    }

    @Override

    public boolean playStorePinWidgets() {
        return getValue(Flags.FLAG_PLAY_STORE_PIN_WIDGETS,
            FeatureFlags::playStorePinWidgets);
    }

    @Override

    public boolean remoteAdapterConversion() {
        return getValue(Flags.FLAG_REMOTE_ADAPTER_CONVERSION,
            FeatureFlags::remoteAdapterConversion);
    }

    @Override

    public boolean remoteDocumentFeatures2025q4() {
        return getValue(Flags.FLAG_REMOTE_DOCUMENT_FEATURES_2025Q4,
            FeatureFlags::remoteDocumentFeatures2025q4);
    }

    @Override

    public boolean remoteDocumentSupport() {
        return getValue(Flags.FLAG_REMOTE_DOCUMENT_SUPPORT,
            FeatureFlags::remoteDocumentSupport);
    }

    @Override

    public boolean remoteViewsProto() {
        return getValue(Flags.FLAG_REMOTE_VIEWS_PROTO,
            FeatureFlags::remoteViewsProto);
    }

    @Override

    public boolean removeAppWidgetServiceIoFromCriticalPath() {
        return getValue(Flags.FLAG_REMOVE_APP_WIDGET_SERVICE_IO_FROM_CRITICAL_PATH,
            FeatureFlags::removeAppWidgetServiceIoFromCriticalPath);
    }

    @Override

    public boolean securityPolicyInteractAcrossUsers() {
        return getValue(Flags.FLAG_SECURITY_POLICY_INTERACT_ACROSS_USERS,
            FeatureFlags::securityPolicyInteractAcrossUsers);
    }

    @Override

    public boolean supportResumeRestoreAfterReboot() {
        return getValue(Flags.FLAG_SUPPORT_RESUME_RESTORE_AFTER_REBOOT,
            FeatureFlags::supportResumeRestoreAfterReboot);
    }

    @Override

    public boolean throttleWidgetUpdates() {
        return getValue(Flags.FLAG_THROTTLE_WIDGET_UPDATES,
            FeatureFlags::throttleWidgetUpdates);
    }

    @Override

    public boolean useSmallerAppWidgetSystemRadius() {
        return getValue(Flags.FLAG_USE_SMALLER_APP_WIDGET_SYSTEM_RADIUS,
            FeatureFlags::useSmallerAppWidgetSystemRadius);
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
            Flags.FLAG_DRAW_DATA_PARCEL,
            Flags.FLAG_ENGAGEMENT_METRICS,
            Flags.FLAG_GENERATED_PREVIEWS,
            Flags.FLAG_NOT_KEYGUARD_CATEGORY,
            Flags.FLAG_PLAY_STORE_PIN_WIDGETS,
            Flags.FLAG_REMOTE_ADAPTER_CONVERSION,
            Flags.FLAG_REMOTE_DOCUMENT_FEATURES_2025Q4,
            Flags.FLAG_REMOTE_DOCUMENT_SUPPORT,
            Flags.FLAG_REMOTE_VIEWS_PROTO,
            Flags.FLAG_REMOVE_APP_WIDGET_SERVICE_IO_FROM_CRITICAL_PATH,
            Flags.FLAG_SECURITY_POLICY_INTERACT_ACROSS_USERS,
            Flags.FLAG_SUPPORT_RESUME_RESTORE_AFTER_REBOOT,
            Flags.FLAG_THROTTLE_WIDGET_UPDATES,
            Flags.FLAG_USE_SMALLER_APP_WIDGET_SYSTEM_RADIUS
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            Flags.FLAG_NOT_KEYGUARD_CATEGORY,
            Flags.FLAG_USE_SMALLER_APP_WIDGET_SYSTEM_RADIUS,
            ""
        )
    );
}
