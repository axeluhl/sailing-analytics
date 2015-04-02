package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.AbstractBaseActivity;
import com.sap.sailing.android.shared.ui.dialogs.AboutDialog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class BaseActivity extends AbstractBaseActivity {

    private static final String TAG = BaseActivity.class.getName();
    protected AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_menu_settings:
                ExLog.i(this, TAG, "Clicked SETTINGS.");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.options_menu_info:
                ExLog.i(this, TAG, "Clicked INFO.");
                AboutDialog dialog = new AboutDialog(this);
                dialog.show();
                // startActivity(new Intent(this, SystemInformationActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getOptionsMenuResId() {
        return R.menu.options_menu;
    }
}
