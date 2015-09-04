package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.PreferenceActivity;

/**
 * <p>
 * This preference fragment can be redirected to display the {@link RegattaConfiguration} of a specifc regatta.
 * </p>
 * <p/>
 * <p/>
 * See {@link PreferenceActivity}.
 */
public class RegattaPreferenceFragment extends BasePreferenceFragment {

    private boolean isRedirected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof PreferenceActivity) {
            PreferenceActivity settingsActivity = (PreferenceActivity) activity;
            isRedirected = settingsActivity.isRedirected();
        }
        if (isRedirected) {
            addPreferencesFromResource(R.xml.preference_regatta_specific);
        }
        addPreferencesFromResource(R.xml.preference_regatta_defaults);

        if (isRedirected) {
            setupSaveButton();
        }
        setupGeneral();
        setupRRS26();
        setupGateStart();
        setupESS();
        setupBasic();
        setupLeague();

        bindPreferenceSummaryToValue(findPreference(R.string.preference_racing_procedure_rrs26_classflag_key));
        bindPreferenceSummaryToSet(findPreference(R.string.preference_racing_procedure_rrs26_startmode_flags_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_racing_procedure_gatestart_classflag_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_racing_procedure_ess_classflag_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_racing_procedure_basic_classflag_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_racing_procedure_league_classflag_key));
    }

    private void setupSaveButton() {
        final String preferencesName = getArguments().getString(PreferenceActivity.EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME);
        final String raceGroupName = getArguments().getString(PreferenceActivity.EXTRA_SPECIFIC_REGATTA_NAME);

        Preference preference = findPreference(R.string.preference_regatta_specific_save_key);

        preference.setTitle(getString(R.string.preference_regatta_specific_save_title, raceGroupName));
        preference.setSummary(getString(R.string.preference_regatta_specific_save_description, raceGroupName));
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PreferenceActivity.commitSpecificRegattaConfiguration(getActivity(), preferencesName, raceGroupName);
                getActivity().finish();
                return false;
            }
        });
    }

    private void setupRRS26() {
        setupRRS26StartmodeFlagsList();
        setupClassFlagList(this.<ListPreference>findPreference(R.string.preference_racing_procedure_rrs26_classflag_key));
    }

    private void setupGateStart() {
        setupClassFlagList(this.<ListPreference>findPreference(R.string.preference_racing_procedure_gatestart_classflag_key));
    }

    private void setupESS() {
        setupClassFlagList(this.<ListPreference>findPreference(R.string.preference_racing_procedure_ess_classflag_key));
    }

    private void setupBasic() {
        setupClassFlagList(this.<ListPreference>findPreference(R.string.preference_racing_procedure_basic_classflag_key));
    }

    private void setupLeague() {
        setupClassFlagList(this.<ListPreference>findPreference(R.string.preference_racing_procedure_league_classflag_key));
    }

    private void setupGeneral() {
        setupRacingProcedureTypePreference();
        setupCourseDesignerTypePreference();
        setupDependentRacesPreference();
    }

    private void setupRacingProcedureTypePreference() {
        final ListPreference startProcedurePreference = findPreference(R.string.preference_racing_procedure_override_key);

        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();
        for (RacingProcedureType type : RacingProcedureType.validValues()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }

        startProcedurePreference.setEntries(entries.toArray(new String[entries.size()]));
        startProcedurePreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));

        bindPreferenceSummaryToValue(startProcedurePreference);
    }

    private void setupCourseDesignerTypePreference() {
        final ListPreference preference = findPreference(R.string.preference_course_designer_override_key);

        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();
        for (CourseDesignerMode type : CourseDesignerMode.validValues()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }

        preference.setEntries(entries.toArray(new String[entries.size()]));
        preference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));

        bindPreferenceSummaryToValue(preference);
    }

    private void setupDependentRacesPreference() {
        bindPreferenceSummaryToInteger(findPreference(R.string.preference_dependent_races_offset_key));
    }

    private void setupRRS26StartmodeFlagsList() {
        MultiSelectListPreference preference = findPreference(R.string.preference_racing_procedure_rrs26_startmode_flags_key);
        List<String> flags = new ArrayList<>();
        for (Flags flag : Flags.validValues()) {
            flags.add(flag.name());
        }

        Collections.sort(flags);
        preference.setEntries(flags.toArray(new String[flags.size()]));
        preference.setEntryValues(flags.toArray(new String[flags.size()]));
    }

    private void setupClassFlagList(ListPreference preference) {
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();
        for (Flags flag : Flags.validValues()) {
            entries.add(flag.toString());
            entryValues.add(flag.name());
        }

        Collections.sort(entries);
        Collections.sort(entryValues);
        preference.setEntries(entries.toArray(new String[entries.size()]));
        preference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
    }
}
