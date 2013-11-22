package com.sap.sailing.racecommittee.app.ui.fragments.preference;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.R;

public class GeneralPreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        
        setupLanguageButton();
        setupServerUrlBox();
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_server_url_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_course_areas_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_mail_key)));
    }

    private void setupServerUrlBox() {
        Preference preference = findPreference(getString(R.string.preference_server_url_key));
        addOnPreferenceChangeListener(preference, new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), 
                        getString(R.string.settings_please_reload), Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private void setupLanguageButton() {
        Preference preference = findPreference(getString(R.string.preference_language_key));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) { 
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");            
                startActivity(intent);
                preference.getOnPreferenceChangeListener().onPreferenceChange(preference, null);
                return true;
            }
        });
    }
}
