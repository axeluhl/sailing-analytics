package com.sap.sailing.racecommittee.app.ui.fragments.preference;


import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;

public class RacingProcedurePreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_racing_procedure);
        
        setupRacingProcedurePreferences();
    }

    private void setupRacingProcedurePreferences() {
        CheckBoxPreference overrideStartProcedurePreference = (CheckBoxPreference) 
                findPreference(getString(R.string.preference_racing_procedure_is_overridden_key));
        final ListPreference startProcedurePreference = (ListPreference) 
                findPreference(getString(R.string.preference_racing_procedure_override_key));
        
        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (RacingProcedureType type : RacingProcedureType.validValues()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }
        
        startProcedurePreference.setEntries(entries.toArray(new CharSequence[0]));
        startProcedurePreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
        
        bindPreferenceToCheckbox(overrideStartProcedurePreference, startProcedurePreference);
        bindPreferenceSummaryToValue(startProcedurePreference);
    }
}
