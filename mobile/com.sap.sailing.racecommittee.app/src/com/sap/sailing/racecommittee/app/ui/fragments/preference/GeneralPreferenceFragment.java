package com.sap.sailing.racecommittee.app.ui.fragments.preference;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;

import com.sap.sailing.racecommittee.app.R;

public class GeneralPreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        
        setupLanguageButton();
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_server_url_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_course_areas_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_mail_key)));
    }

    private void setupLanguageButton() {
        Preference button = findPreference(getString(R.string.preference_language_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) { 
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");            
                startActivity(intent);
                return true;
            }
        });
    }
}
