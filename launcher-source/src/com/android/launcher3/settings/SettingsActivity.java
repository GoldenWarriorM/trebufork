/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.settings;

import static android.os.Process.myUserHandle;
import static android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED;

import static androidx.preference.PreferenceFragmentCompat.ARG_PREFERENCE_ROOT;

import static com.android.launcher3.BuildConfig.IS_DEBUG_DEVICE;
import static com.android.launcher3.BuildConfig.IS_STUDIO_BUILD;
import static com.android.launcher3.InvariantDeviceProfile.TYPE_MULTI_DISPLAY;
import static com.android.launcher3.InvariantDeviceProfile.TYPE_TABLET;
import static com.android.launcher3.states.RotationHelper.ALLOW_ROTATION_PREFERENCE_KEY;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback;
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartScreenCallback;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroup.PreferencePositionCallback;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import com.android.settingslib.widget.SettingsThemeHelper;

import com.android.launcher3.AnimationSpeed;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.Flags;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.lineage.LineageUtils;
import com.android.launcher3.lineage.trust.TrustAppsActivity;
import com.android.launcher3.RemoveAnimationSettingsTracker;
import com.android.launcher3.states.RotationHelper;
import com.android.launcher3.util.DisplayController;
import com.android.launcher3.util.SettingsCache;
import com.google.android.material.appbar.CollapsingToolbarLayout;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends FragmentActivity
        implements OnPreferenceStartFragmentCallback, OnPreferenceStartScreenCallback {

    @VisibleForTesting
    static final String DEVELOPER_OPTIONS_KEY = "pref_developer_options";

    public static final String FIXED_LANDSCAPE_MODE = "pref_fixed_landscape_mode";

    /** trebufork: launcher master animation speed (0.5x..2x), stored as integer percent string. */
    public static final String ANIMATION_SPEED_KEY = "pref_animation_speed";

    private static final String NOTIFICATION_DOTS_PREFERENCE_KEY = "pref_icon_badging";

    public static final String EXTRA_FRAGMENT_ARGS = ":settings:fragment_args";

    // Intent extra to indicate the pref-key to highlighted when opening the settings activity
    public static final String EXTRA_FRAGMENT_HIGHLIGHT_KEY = ":settings:fragment_args_key";
    // Intent extra to indicate the pref-key of the root screen when opening the settings activity
    public static final String EXTRA_FRAGMENT_ROOT_KEY = ARG_PREFERENCE_ROOT;

    private static final int DELAY_HIGHLIGHT_DURATION_MILLIS = 600;
    public static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";

    private static final String KEY_MINUS_ONE = "pref_enable_minus_one";
    private static final String SEARCH_PACKAGE = "com.google.android.googlequicksearchbox";
    public static final String KEY_TRUST_APPS = "pref_trust_apps";

    private static final String KEY_SUGGESTIONS = "pref_suggestions";
    private static final String SUGGESTIONS_PACKAGE = "com.google.android.as";

    private void logTitles(String where) {
        try {
            StringBuilder sb = new StringBuilder(where).append(":");
            ComponentName cn = getComponentName();
            PackageManager pm = getPackageManager();
            int labelRes = 0;
            CharSequence label = null;
            try {
                android.content.pm.ActivityInfo ai = pm.getActivityInfo(cn, 0);
                labelRes = ai.labelRes;
                label = ai.loadLabel(pm);
            } catch (Exception e) { sb.append(" labelErr=").append(e.toString()); }
            sb.append(" mTitle=").append(getTitle())
                    .append(" labelRes=").append(labelRes)
                    .append(" label=").append(label)
                    .append(" actBarTitle=").append(getActionBar() != null ? getActionBar().getTitle() : "null");
            View collapsing = findViewById(R.id.collapsing_toolbar);
            if (collapsing instanceof CollapsingToolbarLayout) {
                sb.append(" collapsingTitle=").append(((CollapsingToolbarLayout) collapsing).getTitle());
            } else {
                sb.append(" collapsingView=").append(collapsing);
            }
            Log.d("TREBUFORK_DIAG", sb.toString());
        } catch (Throwable t) {
            Log.d("TREBUFORK_DIAG", where + " ERR " + t, t);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // DeviceDefault.Settings installs a decor action bar on this device even
        // though the theme requests a Toolbar. Reuse the XML Toolbar for the
        // collapsing layout and hide the unused decor bar instead of calling
        // setActionBar() a second time (which crashes on Android 16).
        android.widget.Toolbar toolbar = findViewById(R.id.action_bar);
        android.app.ActionBar decorActionBar = getActionBar();
        if (decorActionBar != null) {
            decorActionBar.hide();
        } else {
            setActionBar(toolbar);
        }
        toolbar.setTitle(R.string.settings_button_text);
        // PackageManager can retain the activity label resource from an older
        // system APK after a Magisk overlay is updated in place. Resolve the
        // title from the current resource table instead of relying on that
        // cached ActivityInfo label.
        setTitle(R.string.settings_button_text);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        logTitles("SettingsActivity.onCreate");

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FRAGMENT_ROOT_KEY) || intent.hasExtra(EXTRA_FRAGMENT_ARGS)
                || intent.hasExtra(EXTRA_FRAGMENT_HIGHLIGHT_KEY)) {
            if (decorActionBar != null) {
                decorActionBar.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        if (savedInstanceState == null) {
            Bundle args = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS);
            if (args == null) {
                args = new Bundle();
            }

            String highlight = intent.getStringExtra(EXTRA_FRAGMENT_HIGHLIGHT_KEY);
            if (!TextUtils.isEmpty(highlight)) {
                args.putString(EXTRA_FRAGMENT_HIGHLIGHT_KEY, highlight);
            }
            String root = intent.getStringExtra(EXTRA_FRAGMENT_ROOT_KEY);
            if (!TextUtils.isEmpty(root)) {
                args.putString(EXTRA_FRAGMENT_ROOT_KEY, root);
            }

            final FragmentManager fm = getSupportFragmentManager();
            final Fragment f = fm.getFragmentFactory().instantiate(getClassLoader(),
                    getString(R.string.settings_fragment_name));
            f.setArguments(args);
            // Display the fragment as the main content.
            fm.beginTransaction().replace(R.id.content_frame, f).commit();
        }
    }

    private boolean startPreference(String fragment, Bundle args, String key) {
        if (getSupportFragmentManager().isStateSaved()) {
            // Sometimes onClick can come after onPause because of being posted on the handler.
            // Skip starting new preferences in that case.
            return false;
        }
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.getFragmentFactory().instantiate(getClassLoader(), fragment);
        if (f instanceof DialogFragment) {
            f.setArguments(args);
            ((DialogFragment) f).show(fm, key);
        } else {
            startActivity(new Intent(this, SettingsActivity.class)
                    .putExtra(EXTRA_FRAGMENT_ARGS, args));
        }
        return true;
    }

    @Override
    public boolean onPreferenceStartFragment(
            PreferenceFragmentCompat preferenceFragment, Preference pref) {
        return startPreference(pref.getFragment(), pref.getExtras(), pref.getKey());
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        Bundle args = new Bundle();
        args.putString(ARG_PREFERENCE_ROOT, pref.getKey());
        return startPreference(getString(R.string.settings_fragment_name), args, pref.getKey());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if (SettingsThemeHelper.isExpressiveTheme(this)) {
            theme.applyStyle(
                    com.android.settingslib.widget.theme.R.style.Theme_SubSettingsBase_Expressive,
                    true);
        }
        return theme;
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends SettingsBasePreferenceFragment implements
            SettingsCache.OnChangeListener {

        protected boolean mDeveloperOptionsEnabled = false;

        private boolean mRestartOnResume = false;

        private String mHighLightKey;

        private boolean mPreferenceHighlighted = false;

        /**
         * trebufork: observers the three Developer-options animation scales. When any of them is
         * changed away from 1x, the launcher animation-speed control becomes disabled (the system
         * override takes precedence).
         */
        private final ContentObserver mAnimationScaleObserver =
                new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange, @Nullable Uri uri) {
                        if (!isAdded()) return;
                        updateAnimationSpeedPreference(findPreference(ANIMATION_SPEED_KEY));
                    }
                };

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            if (BuildConfig.IS_DEBUG_DEVICE) {
                Uri devUri = Settings.Global.getUriFor(DEVELOPMENT_SETTINGS_ENABLED);
                SettingsCache settingsCache = SettingsCache.INSTANCE.get(getContext());
                mDeveloperOptionsEnabled = settingsCache.getValue(devUri);
                settingsCache.register(devUri, this);
            }
            if (getContext() != null) {
                ContentResolver cr = getContext().getContentResolver();
                cr.registerContentObserver(Settings.Global.getUriFor(
                        Settings.Global.WINDOW_ANIMATION_SCALE), false, mAnimationScaleObserver);
                cr.registerContentObserver(Settings.Global.getUriFor(
                        Settings.Global.TRANSITION_ANIMATION_SCALE), false, mAnimationScaleObserver);
                cr.registerContentObserver(Settings.Global.getUriFor(
                        Settings.Global.ANIMATOR_DURATION_SCALE), false, mAnimationScaleObserver);
            }
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            final Bundle args = getArguments();
            mHighLightKey = args == null ? null : args.getString(EXTRA_FRAGMENT_HIGHLIGHT_KEY);

            if (savedInstanceState != null) {
                mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY);
            }

            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            setPreferencesFromResource(R.xml.launcher_preferences, rootKey);

            PreferenceScreen screen = getPreferenceScreen();
            for (int i = screen.getPreferenceCount() - 1; i >= 0; i--) {
                Preference preference = screen.getPreference(i);
                if (!initPreference(preference)) {
                    screen.removePreference(preference);
                }
            }

            // trebufork: master animation speed - reflect enablement/summary based on whether the
            // Developer-options animation scales are overridden.
            updateAnimationSpeedPreference(screen.findPreference(ANIMATION_SPEED_KEY));

            // If the target preference is not in the current preference screen, find the parent
            // preference screen that contains the target preference and set it as the preference
            // screen.
            if (mHighLightKey != null
                    && !isKeyInPreferenceGroup(mHighLightKey, screen)) {
                final PreferenceScreen parentPreferenceScreen =
                        findParentPreference(screen, mHighLightKey);
                if (parentPreferenceScreen != null && getActivity() != null) {
                    if (!TextUtils.isEmpty(parentPreferenceScreen.getTitle())) {
                        getActivity().setTitle(parentPreferenceScreen.getTitle());
                    }
                    setPreferenceScreen(parentPreferenceScreen);
                    return;
                }
            }

            if (getActivity() != null && !TextUtils.isEmpty(getPreferenceScreen().getTitle())) {
                getActivity().setTitle(getPreferenceScreen().getTitle());
            }
            ((SettingsActivity) getActivity()).logTitles(
                    "LauncherSettingsFragment.onCreatePreferences");
        }

        private boolean isKeyInPreferenceGroup(String targetKey, PreferenceGroup parent) {
            for (int i = 0; i < parent.getPreferenceCount(); i++) {
                Preference pref = parent.getPreference(i);
                if (pref.getKey() != null && pref.getKey().equals(targetKey)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Finds the parent preference screen for the given target key.
         *
         * @param parent    the parent preference screen
         * @param targetKey the key of the preference to find
         * @return the parent preference screen that contains the target preference
         */
        @Nullable
        private PreferenceScreen findParentPreference(PreferenceScreen parent, String targetKey) {
            for (int i = 0; i < parent.getPreferenceCount(); i++) {
                Preference pref = parent.getPreference(i);
                if (pref instanceof PreferenceScreen) {
                    PreferenceScreen foundKey = findParentPreference((PreferenceScreen) pref,
                            targetKey);
                    if (foundKey != null) {
                        return foundKey;
                    }
                } else if (pref.getKey() != null && pref.getKey().equals(targetKey)) {
                    return parent;
                }
            }
            return null;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            RecyclerView preferenceList = getListView();
            preferenceList.addOnChildAttachStateChangeListener(
                    new RecyclerView.OnChildAttachStateChangeListener() {
                        @Override
                        public void onChildViewAttachedToWindow(
                                @androidx.annotation.NonNull View child) {
                            normalizePreferenceTextWidth(child);
                        }

                        @Override
                        public void onChildViewDetachedFromWindow(
                                @androidx.annotation.NonNull View child) {
                        }
                    });
            for (int i = 0; i < preferenceList.getChildCount(); i++) {
                normalizePreferenceTextWidth(preferenceList.getChildAt(i));
            }
        }

        /**
         * The standalone AndroidX resource merger leaves the SettingsLib text frame at its
         * wrap-content width. Soong's static resource graph measures it against the remaining row
         * width. Restore only that width contract; text appearance, colors and row dimensions stay
         * owned by SettingsLib and the active system theme.
         */
        private void normalizePreferenceTextWidth(View row) {
            TextView title = row.findViewById(android.R.id.title);
            TextView summary = row.findViewById(android.R.id.summary);
            if (title != null) {
                ViewGroup.LayoutParams params = title.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                title.setLayoutParams(params);
            }
            if (summary != null) {
                ViewGroup.LayoutParams params = summary.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                summary.setLayoutParams(params);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mPreferenceHighlighted);
        }

        /**
         * Initializes a preference. This is called for every preference. Returning false here
         * will remove that preference from the list.
         */
        protected boolean initPreference(Preference preference) {
            DisplayController.Info info = DisplayController.INSTANCE.get(getContext()).getInfo();
            LauncherApps launcherApps = getContext().getSystemService(LauncherApps.class);
            switch (preference.getKey()) {
                case NOTIFICATION_DOTS_PREFERENCE_KEY:
                    return BuildConfig.NOTIFICATION_DOTS_ENABLED;
                case ALLOW_ROTATION_PREFERENCE_KEY:
                    if (Flags.oneGridSpecs() && !info.isRotationAllowed()) {
                        return false;
                    }
                    if (info.isTablet(info.realBounds)) {
                        // Launcher supports rotation by default. No need to show this setting.
                        return false;
                    }
                    // Initialize the UI once
                    preference.setDefaultValue(RotationHelper.getAllowRotationDefaultValue(info));
                    return true;
                case DEVELOPER_OPTIONS_KEY:
                    if (IS_STUDIO_BUILD) {
                        preference.setOrder(0);
                    }
                    return mDeveloperOptionsEnabled;
                case FIXED_LANDSCAPE_MODE:
                    if (!Flags.oneGridSpecs()
                            // adding this condition until fixing b/378972567
                            || InvariantDeviceProfile.INSTANCE.get(getContext()).deviceType
                            == TYPE_MULTI_DISPLAY
                            || InvariantDeviceProfile.INSTANCE.get(getContext()).deviceType
                            == TYPE_TABLET
                            || info.isRotationAllowed()) {
                        return false;
                    }
                    // When the setting changes rotate the screen accordingly to showcase the result
                    // of the setting
                    preference.setOnPreferenceChangeListener(
                            (pref, newValue) -> {
                                getActivity().setRequestedOrientation(
                                        (boolean) newValue
                                                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                                : ActivityInfo.SCREEN_ORIENTATION_USER
                                );
                                return true;
                            }
                    );
                    return !info.isTablet(info.realBounds);
                case KEY_MINUS_ONE:
                    return launcherApps != null &&
                            launcherApps.isPackageEnabled(SEARCH_PACKAGE, myUserHandle());
                case KEY_TRUST_APPS:
                    preference.setOnPreferenceClickListener(p -> {
                        LineageUtils.showLockScreen(getActivity(),
                                getString(R.string.trust_apps_manager_name), () -> {
                            Intent intent = new Intent(getActivity(), TrustAppsActivity.class);
                            startActivity(intent);
                        });
                        return true;
                    });
                    return true;
                case KEY_SUGGESTIONS:
                    return launcherApps != null &&
                            launcherApps.isPackageEnabled(SUGGESTIONS_PACKAGE, myUserHandle());
            }
            return true;
        }

        @Override
        public void onResume() {
            super.onResume();
            ((SettingsActivity) getActivity()).logTitles("LauncherSettingsFragment.onResume");

            if (isAdded() && !mPreferenceHighlighted) {
                PreferenceHighlighter highlighter = createHighlighter();
                if (highlighter != null) {
                    getView().postDelayed(highlighter, DELAY_HIGHLIGHT_DURATION_MILLIS);
                    mPreferenceHighlighted = true;
                }
            }

            if (mRestartOnResume) {
                recreateActivityNow();
            }
        }

        @Override
        public void onSettingsChanged(boolean isEnabled) {
            // Developer options changed, try recreate
            tryRecreateActivity();
        }

        private void updateAnimationSpeedPreference(@Nullable Preference pref) {
            if (pref == null || getContext() == null) return;
            if (AnimationSpeed.isOverriddenByDeveloperOptions(getContext())) {
                pref.setEnabled(false);
                pref.setSummary(R.string.pref_animation_speed_summary_off);
            } else {
                pref.setEnabled(true);
                String label = "1x";
                if (pref instanceof ListPreference) {
                    ListPreference lp = (ListPreference) pref;
                    String value = lp.getValue();
                    int index = lp.findIndexOfValue(value == null ? "100" : value);
                    if (index >= 0 && index < lp.getEntries().length) {
                        label = lp.getEntries()[index].toString();
                    }
                }
                pref.setSummary(getString(R.string.pref_animation_speed_summary_on, label));
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (getContext() != null) {
                getContext().getContentResolver().unregisterContentObserver(mAnimationScaleObserver);
            }
            if (IS_DEBUG_DEVICE) {
                SettingsCache.INSTANCE.get(getContext())
                        .unregister(Settings.Global.getUriFor(DEVELOPMENT_SETTINGS_ENABLED), this);
            }
        }

        /**
         * Tries to recreate the preference
         */
        protected void tryRecreateActivity() {
            if (isResumed()) {
                recreateActivityNow();
            } else {
                mRestartOnResume = true;
            }
        }

        private void recreateActivityNow() {
            Activity activity = getActivity();
            if (activity != null) {
                activity.recreate();
            }
        }

        private PreferenceHighlighter createHighlighter() {
            if (TextUtils.isEmpty(mHighLightKey)) {
                return null;
            }

            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return null;
            }

            RecyclerView list = getListView();
            PreferencePositionCallback callback = (PreferencePositionCallback) list.getAdapter();
            int position = callback.getPreferenceAdapterPosition(mHighLightKey);
            return position >= 0 ? new PreferenceHighlighter(
                    list, position, screen.findPreference(mHighLightKey))
                    : null;
        }
    }
}
