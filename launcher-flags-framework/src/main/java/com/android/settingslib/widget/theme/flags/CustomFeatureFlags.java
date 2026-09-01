package com.android.settingslib.widget.theme.flags;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import android.os.Build;
/** @hide */
public class CustomFeatureFlags implements FeatureFlags {

    private BiPredicate<String, Predicate<FeatureFlags>> mGetValueImpl;

    public CustomFeatureFlags(BiPredicate<String, Predicate<FeatureFlags>> getValueImpl) {
        mGetValueImpl = getValueImpl;
    }
    @Override

    public boolean isExpressiveDesignEnabled() {
        return getValue(Flags.FLAG_IS_EXPRESSIVE_DESIGN_ENABLED,
            FeatureFlags::isExpressiveDesignEnabled);
    }


    protected boolean getValue(String flagName, Predicate<FeatureFlags> getter) {
        return mGetValueImpl.test(flagName, getter);
    }

    public List<String> getFlagNames() {
        return Arrays.asList(
            Flags.FLAG_IS_EXPRESSIVE_DESIGN_ENABLED
        );
    }

    private Set<String> mReadOnlyFlagsSet = new HashSet<>(
        Arrays.asList(
            ""
        )
    );
    private Map<String, Boolean> mFinalizedFlags = new HashMap<>(
        Map.ofEntries(
            Map.entry("", false)
        )
    );

    public boolean isFlagFinalized(String flagName) {
        if (!mFinalizedFlags.containsKey(flagName)) {
            return false;
        }
        return mFinalizedFlags.get(flagName);
    }
}
