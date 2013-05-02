package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.racecommittee.app.R;

/**
 * @author Basil Hess (basil.hess@sap.com)
 * 
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings_view);
        
        ListPreference startProcedurePreference = (ListPreference) findPreference("defaultStartProcedureType");
        
        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (StartProcedureType type : StartProcedureType.values()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }
        
        startProcedurePreference.setEntries(entries.toArray(new CharSequence[0]));
        startProcedurePreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
    }
    
}
