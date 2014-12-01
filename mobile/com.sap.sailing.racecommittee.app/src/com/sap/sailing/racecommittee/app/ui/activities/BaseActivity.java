package com.sap.sailing.racecommittee.app.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;

/**
 * Base activity for all race committee cockpit activities enabling basic menu functionality.
 */
public class BaseActivity extends SendingServiceAwareActivity {
    private static final String TAG = BaseActivity.class.getName();
    
    protected AppPreferences preferences;
    
    @Override
    protected int getOptionsMenuResId() {
        return R.menu.options_menu;
    }

    public RaceApplication getRaceApplication() {
        return (RaceApplication) getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = AppPreferences.on(getApplicationContext());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_settings:
            ExLog.i(this, TAG, "Clicked SETTINGS.");
            fadeActivity(PreferenceActivity.class, false);
//            Intent intent = new Intent(this, PreferenceActivity.class);
//            Bundle info = new Bundle();
//            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, RegattaPreferenceFragment.class.getName());
//            info.putString(PreferenceActivity.EXTRA_SPECIFIC_REGATTA_PREFERENCES_NAME, PreferenceActivity.SPECIFIC_REGATTA_PREFERENCES_NAME);
//            info.putString(PreferenceActivity.EXTRA_SPECIFIC_REGATTA_NAME, "Test");
//            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, info);
//            startActivity(intent);

            return true;
        case R.id.options_menu_reload:
            ExLog.i(this, TAG, "Clicked RESET.");
            InMemoryDataStore.INSTANCE.reset();
            return onReset();
        case R.id.options_menu_info:
            ExLog.i(this, TAG, "Clicked INFO.");
            fadeActivity(SystemInformationActivity.class, false);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    protected boolean onReset() {
        fadeActivity(LoginActivity.class, true);
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        preferences = AppPreferences.on(this);
        if (preferences.wakelockEnabled()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
