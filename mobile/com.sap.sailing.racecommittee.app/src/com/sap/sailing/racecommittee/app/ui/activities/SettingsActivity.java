package com.sap.sailing.racecommittee.app.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

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
    }
}
