package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

import com.sap.sailing.racecommittee.app.R;

public class IntegerEditTextPreference extends EditTextPreference {

    public IntegerEditTextPreference(Context context) {
        super(context);
        setDialogLayoutResource(R.layout.integer_preference_dialog);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.integer_preference_dialog);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDialogLayoutResource(R.layout.integer_preference_dialog);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        try {
            return persistInt(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
