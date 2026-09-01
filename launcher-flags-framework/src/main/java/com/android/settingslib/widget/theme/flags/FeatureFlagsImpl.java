package com.android.settingslib.widget.theme.flags;

import android.os.Build;
import android.os.flagging.AconfigPackage;
import android.util.Log;
/** @hide */
public final class FeatureFlagsImpl implements FeatureFlags {
    private static final String TAG = "FeatureFlagsImplExport";
    private static volatile boolean isCached = false;

    private static boolean isExpressiveDesignEnabled = false;
    private void init() {
        try {
            AconfigPackage reader = AconfigPackage.load("com.android.settingslib.widget.theme.flags");
            isExpressiveDesignEnabled = reader.getBooleanFlagValue("is_expressive_design_enabled", false);
        } catch (Exception e) {
            // pass
            Log.e(TAG, e.toString());
        } catch (LinkageError e) {
            // for mainline module running on older devices.
            // This should be replaces to version check, after the version bump.
            Log.w(TAG, e.toString());
        }
        isCached = true;
    }
    @Override
    public boolean isExpressiveDesignEnabled() {
        if (!isCached) {
            synchronized (FeatureFlagsImpl.class) {
                if (!isCached) {
                    init();
                }
            }
        }
        return isExpressiveDesignEnabled;
    }

}
