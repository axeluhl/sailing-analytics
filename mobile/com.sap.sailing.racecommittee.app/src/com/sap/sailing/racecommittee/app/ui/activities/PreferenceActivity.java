package com.sap.sailing.racecommittee.app.ui.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesRegattaConfigurationLoader;
import com.sap.sailing.racecommittee.app.ui.fragments.MainPreferenceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.RegattaPreferenceFragment;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class PreferenceActivity extends AppCompatActivity {

    public static final String SPECIFIC_REGATTA_PREFERENCES_NAME = "TEMP_PREFERENCE_KEY";
    public static final String EXTRA_SPECIFIC_REGATTA_NAME = "EXTRA_SPECIFIC_REGATTA_NAME";
    public static final String EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME = "EXTRA_SPECIFIC_REGATTA_PREFERENCE_KEY";
    public static final String EXTRA_SHOW_FRAGMENT = "SHOW_FRAGMENT";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = "SHOW_FRAGMENT_ARGUMENTS";
    private static final String TAG = PreferenceActivity.class.getName();

    public static void openSpecificRegattaConfiguration(Context context, RaceGroup raceGroup) {
        // reset temp preferences
        PreferenceHelper helper = new PreferenceHelper(context, "TEMP_PREFERENCE_KEY");
        helper.clearPreferences();
        helper.resetPreferences(true);

        // store local configuration in temp preferences
        RegattaConfiguration configuration = raceGroup.getRegattaConfiguration();
        AppPreferences preferences = AppPreferences.on(context, "TEMP_PREFERENCE_KEY");
        PreferencesRegattaConfigurationLoader preferencesLoader = new PreferencesRegattaConfigurationLoader(configuration, preferences);
        preferencesLoader.store();

        Intent intent = new Intent(context, PreferenceActivity.class);
        Bundle info = new Bundle();
        intent.putExtra(EXTRA_SHOW_FRAGMENT, RegattaPreferenceFragment.class.getName());
        info.putString(EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME, SPECIFIC_REGATTA_PREFERENCES_NAME);
        info.putString(EXTRA_SPECIFIC_REGATTA_NAME, raceGroup.getName());
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, info);
        context.startActivity(intent);
    }

    public static void commitSpecificRegattaConfiguration(Context context, String preferencesName,
        String raceGroupName) {

        ReadonlyDataManager dataManager = DataManager.create(context);
        RaceGroup group = dataManager.getDataStore().getRaceGroup(raceGroupName);
        if (group != null) {
            RegattaConfiguration localConfiguration = group.getRegattaConfiguration();
            AppPreferences preferences = AppPreferences.on(context, preferencesName);
            PreferencesRegattaConfigurationLoader loader = new PreferencesRegattaConfigurationLoader(localConfiguration, preferences);
            loader.load();
        } else {
            String toastText = context.getString(R.string.no_fitting_race_group_found);
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.setTheme(this);

        setContentView(R.layout.preference_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setTitle(getText(R.string.settings_activity_title));
            }
        }

        Fragment fragment = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.containsKey(EXTRA_SHOW_FRAGMENT)) {
                String className = bundle.getString(EXTRA_SHOW_FRAGMENT);
                try {
                    fragment = (Fragment) Class.forName(className).newInstance();
                } catch (InstantiationException ex) {
                    ExLog.ex(this, TAG, ex);
                } catch (IllegalAccessException ex) {
                    ExLog.ex(this, TAG, ex);
                } catch (ClassNotFoundException ex) {
                    ExLog.ex(this, TAG, ex);
                }
                if (bundle.containsKey(EXTRA_SHOW_FRAGMENT_ARGUMENTS)) {
                    Bundle info = bundle.getBundle(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
                    if (info != null) {
                        // sharedPreferencesName = info.getString(EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME);
                        String raceGroupName = info.getString(EXTRA_SPECIFIC_REGATTA_NAME);
                        String title = getString(R.string.preference_regatta_specific_title, raceGroupName);
                        getSupportActionBar().setTitle(title);
                    }
                }
            }
        }
        if (fragment == null) {
            fragment = MainPreferenceFragment.newInstance();
        }
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.settings_activity_title));
            }
            getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().commit();
        } else {
            super.onBackPressed();
        }
    }

}
