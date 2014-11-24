package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesRegattaConfigurationLoader;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.RegattaPreferenceFragment;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getName();
    


    private boolean isRedirectedToTemp;
    private String sharedPreferencesName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getIntent().getExtras();

        // the additional null-check seems to be necessary (see bug 2377)
        this.isRedirectedToTemp = arguments != null
                && arguments.containsKey(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS)
                && arguments.get(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS) != null;
        if (isRedirectedToTemp) {
            Bundle info = arguments.getBundle(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
            if (info != null) {
//                sharedPreferencesName = info.getString(EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME);
//                String raceGroupName = info.getString(EXTRA_SPECIFIC_REGATTA_NAME);
//                String title = getString(R.string.preference_regatta_specific_title, raceGroupName);
//                showBreadCrumbs(title, title);
            }
        }
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (isRedirectedToTemp) {
            return super.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        }
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public boolean isRedirected() {
        return isRedirectedToTemp;
    }

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
