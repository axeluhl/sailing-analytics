package com.sap.sailing.android.shared.ui.views;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.android.shared.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

public class EditSetPreference extends DialogPreference {

    private static final Set<String> defaultFallbackValue = new HashSet<String>();

    private static final int mDialogLayoutResId = R.layout.edit_set_preference;

    private Set<String> currentValue;

    private Set<String> exampleValues;

    public EditSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.exampleValues = new HashSet<String>();
    }

    public void setExampleValues(String[] values) {
        this.exampleValues.addAll(Arrays.asList(values));
    }

    public Set<String> getExampleValues() {
        return exampleValues;
    }

    public Set<String> getCurrentValue() {
        return currentValue;
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            currentValue = getPersistedStringSet(defaultFallbackValue);
        } else {
            // Set default state from the XML attribute
            currentValue = new HashSet<String>((Set<String>) defaultValue);
            persistStringSet(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> defaultValue = new HashSet<String>();
        for (CharSequence chars : a.getTextArray(index)) {
            defaultValue.add(chars.toString());
        }
        return defaultValue;
    }

    @Override
    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        Set<String> fallbackDefault = defaultReturnValue == null ? null : new HashSet<String>(defaultReturnValue);
        Set<String> value = getSharedPreferences().getStringSet(getKey(), fallbackDefault);

        return value == null ? null : new HashSet<String>(value);
    }

}
