package com.android.settingslib.widget.theme.flags;

import android.os.Build;
/** @hide */
public final class Flags {
    /** @hide */
    public static final String FLAG_IS_EXPRESSIVE_DESIGN_ENABLED = "com.android.settingslib.widget.theme.flags.is_expressive_design_enabled";
    public static boolean isExpressiveDesignEnabled() {
        
        return FEATURE_FLAGS.isExpressiveDesignEnabled();
    }

    private static FeatureFlags FEATURE_FLAGS = new FeatureFlagsImpl();

}
