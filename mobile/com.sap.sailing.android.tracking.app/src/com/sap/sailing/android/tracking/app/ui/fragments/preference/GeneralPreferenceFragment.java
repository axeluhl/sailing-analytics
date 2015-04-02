package com.sap.sailing.android.tracking.app.ui.fragments.preference;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class GeneralPreferenceFragment extends BasePreferenceFragment {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);

        AppPreferences prefs = new AppPreferences(getActivity());
        String currentDeviceId = prefs.getDeviceIdentifier();
        
        Preference deviceIdPreference = (Preference) findPreference(getString(R.string.preference_device_identifier_key));
        
        if (currentDeviceId != null && currentDeviceId.length() > 0)
        {
            deviceIdPreference.setSummary(currentDeviceId);	
        }
        else
        {
        	// this would, in most caes, be set elsewhere, but since we don't want to show an empty summary, 
        	// we can just set it here. (UniqueDeviceUuid.getUniqueId sets it in prefs.)
        	String newDeviceId = UniqueDeviceUuid.getUniqueId(getActivity());
        	deviceIdPreference.setSummary(newDeviceId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        SwitchPreference prefEnergy = (SwitchPreference)findPreference(R.string.preference_energy_saving_enabled_key);
        SwitchPreference prefDeclination = (SwitchPreference)findPreference(R.string.preference_heading_with_declination_subtracted_key);

        AppPreferences prefs = new AppPreferences(getActivity());

        prefs.setEnergySavingEnabledByUser(prefEnergy.isChecked());
        prefs.setDisplayHeadingWithSubtractedDeclination(prefDeclination.isChecked());
    }
}
