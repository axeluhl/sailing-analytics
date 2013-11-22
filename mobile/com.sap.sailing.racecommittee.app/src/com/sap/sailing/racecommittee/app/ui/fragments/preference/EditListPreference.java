package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class EditListPreference extends DialogPreference {

    private static final String TAG = EditListPreference.class.getName();
    
    private static final Set<String> defaultFallbackValue = new HashSet<String>();
    private Set<String> currentValue;
    
    public EditListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setDialogLayoutResource(R.layout.race_choose_line_opening_time_view);
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            currentValue = getPersistedStringSet(defaultFallbackValue);
        } else {
            // Set default state from the XML attribute
            currentValue = (Set<String>) defaultValue;
            persistStringSet(currentValue);
        }
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return toSet(a.getString(index));
    }
    
    protected static Set<String> toSet(String csl) {
        if (csl == null || csl.isEmpty()) {
            return new HashSet<String>();
        }
        
        String[] values = csl.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        
        return new HashSet<String>(Arrays.asList(values));
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            ExLog.i(TAG, "Storing result...");
            persistStringSet(currentValue);
        }
    }

    private boolean persistStringSet(Set<String> value) {
        if (shouldPersist()) {
            // Shouldn't store null
            if (value == getPersistedStringSet(null)) {
                // It's already there, so the same as persisting
                return true;
            }
            
            SharedPreferences.Editor editor = getEditor();
            editor.putStringSet(getKey(), value);
            if (shouldCommit()) {
                editor.commit();
            }
            
            return true;
        }
        return false;
    }
    
    protected Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        
        return getSharedPreferences().getStringSet(getKey(), defaultReturnValue);
    }


}
