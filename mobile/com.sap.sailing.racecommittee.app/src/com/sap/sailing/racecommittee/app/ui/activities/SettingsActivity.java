package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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
        
        setupStartProcedurePreferences();
        setupLanguageButton();
    }

    private void setupStartProcedurePreferences() {
        final ListPreference startProcedurePreference = (ListPreference) findPreference("defaultStartProcedureType");
        
        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (RacingProcedureType type : RacingProcedureType.values()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }
        
        startProcedurePreference.setEntries(entries.toArray(new CharSequence[0]));
        startProcedurePreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
        
        CheckBoxPreference overrideStartProcedurePreference = (CheckBoxPreference) findPreference("overrideDefaultStartProcedureType");
        startProcedurePreference.setEnabled(overrideStartProcedurePreference.isChecked());
        
        overrideStartProcedurePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                startProcedurePreference.setEnabled(isChecked.booleanValue());
                return true;
            }
        });
    }

    private void setupLanguageButton() {
        Preference button = (Preference)findPreference("languagePref");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");            
                startActivity(intent);
                return true;
            }
        });
    }
    
}
