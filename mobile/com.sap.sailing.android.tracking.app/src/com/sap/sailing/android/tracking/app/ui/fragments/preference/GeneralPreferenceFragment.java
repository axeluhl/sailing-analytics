package com.sap.sailing.android.tracking.app.ui.fragments.preference;


import android.os.Bundle;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.tracking.app.R;

public class GeneralPreferenceFragment extends BasePreferenceFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        bindPreferenceSummaryToValue(findPreference(R.string.preference_device_identifier_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_server_url_key));
        bindPreferenceSummaryToValue(findPreference(R.string.preference_gps_fix_interval_ms_key));
    }
}
