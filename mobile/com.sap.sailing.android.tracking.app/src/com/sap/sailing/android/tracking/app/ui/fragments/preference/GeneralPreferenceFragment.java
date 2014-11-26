package com.sap.sailing.android.tracking.app.ui.fragments.preference;


import android.os.Bundle;
import android.preference.Preference;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.UniqueDeviceUuid;

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
 
}
