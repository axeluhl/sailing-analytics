package com.sap.sailing.android.tracking.app.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.SendingServiceAwareActivity;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class BaseActivity extends SendingServiceAwareActivity {
    private static final String TAG = SendingServiceAwareActivity.class.getName();
    protected AppPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
    }

    @Override
    protected int getOptionsMenuResId() {
        return R.menu.options_menu;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_settings:
            ExLog.i(this, TAG, "Clicked SETTINGS.");
            fadeActivity(SettingsActivity.class, false);
            return true;
        case R.id.options_menu_info:
            ExLog.i(this, TAG, "Clicked INFO.");
            fadeActivity(SystemInformationActivity.class, false);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
