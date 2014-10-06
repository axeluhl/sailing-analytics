package com.sap.sailing.racecommittee.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.SendingServiceAwareActivity;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = AppPreferences.on(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_settings:
            ExLog.i(this, TAG, "Clicked SETTINGS.");
            fadeActivity(SettingsActivity.class, false);
            return true;
        case R.id.options_menu_reload:
            ExLog.i(this, TAG, "Clicked RESET.");
            InMemoryDataStore.INSTANCE.reset();
            return onReset();
        case R.id.options_menu_info:
            ExLog.i(this, TAG, "Clicked INFO.");
            fadeActivity(SystemInformationActivity.class, false);
            return true;
        case android.R.id.home:
            ExLog.i(this, TAG, "Clicked HOME.");
            return onHomeClicked();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onHomeClicked() {
        return false;
    }

    protected boolean onReset() {
        fadeActivity(LoginActivity.class, true);
        return true;
    }

    protected void fadeActivity(Class<?> activity, boolean newTopTask) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (newTopTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        fadeActivity(intent);
    }

    protected void fadeActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    public RaceApplication getRaceApplication() {
        return (RaceApplication) getApplication();
    }

    @Override
    protected int getOptionsMenuResId() {
        return R.menu.options_menu;
    }
}
