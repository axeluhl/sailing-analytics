package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.List;

import com.sap.sailing.racecommittee.app.R;

import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
}
