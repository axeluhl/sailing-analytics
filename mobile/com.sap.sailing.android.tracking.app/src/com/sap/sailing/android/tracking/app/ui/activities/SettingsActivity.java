package com.sap.sailing.android.tracking.app.ui.activities;

import java.util.List;

import android.preference.PreferenceActivity;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.preference.GeneralPreferenceFragment;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
    
    /* (non-javadoc)
     * Seems to be new for this target API level, fixing a security hole.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }
}
