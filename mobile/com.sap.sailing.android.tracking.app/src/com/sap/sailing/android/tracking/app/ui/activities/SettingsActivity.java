package com.sap.sailing.android.tracking.app.ui.activities;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.sap.sailing.android.tracking.app.ui.fragments.preference.GeneralPreferenceFragment;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public void onBuildHeaders(List<Header> target) {
        //loadHeadersFromResource(R.xml.preference_headers, target);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GeneralPreferenceFragment()).commit();
    }
    
    /* (non-javadoc)
     * Seems to be new for this target API level, fixing a security hole.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }
}
