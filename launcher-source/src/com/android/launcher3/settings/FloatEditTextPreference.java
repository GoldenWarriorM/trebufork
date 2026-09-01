/*
 * Copyright (C) 2026 The trebufork Project
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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

/**
 * An {@link EditTextPreference} that persists its value as a float and shows a decimal keypad, so
 * the scrollable-home alphabet values can be typed precisely instead of using sliders.
 */
public class FloatEditTextPreference extends EditTextPreference {

    public FloatEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatEditTextPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnBindEditTextListener(editText -> editText.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED));
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        // AndroidX passes null here when the value is already persisted (restore mode), so the
        // fallback must be resolved defensively rather than parsed unconditionally.
        float fallback = 0f;
        if (defaultReturnValue != null) {
            try {
                fallback = Float.parseFloat(defaultReturnValue);
            } catch (NumberFormatException e) {
                return defaultReturnValue;
            }
        }
        return format(readPersistedFloat(fallback));
    }

    @Override
    protected boolean persistString(String value) {
        if (value == null) {
            return false;
        }
        try {
            return persistFloat(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Reads the persisted value, tolerating a legacy integer stored by the old slider. */
    private float readPersistedFloat(float defaultValue) {
        SharedPreferences prefs = getSharedPreferences();
        Object value = prefs.getAll().get(getKey());
        return value instanceof Number ? ((Number) value).floatValue() : defaultValue;
    }

    /** Formats a float without a trailing ".0" so whole numbers read naturally. */
    private static String format(float value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
