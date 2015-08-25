package com.sap.sailing.racecommittee.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppUtils.lockOrientation(this);
        ThemeHelper.setTheme(this);

        this.preferences = AppPreferences.on(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.options_menu_settings:
                ExLog.i(this, TAG, "Clicked SETTINGS");
                intent = new Intent(this, PreferenceActivity.class);
                startActivity(intent);
                return true;

            case R.id.options_menu_info:
                ExLog.i(this, TAG, "Clicked INFO");
                intent = new Intent(this, SystemInformationActivity.class);
                startActivity(intent);
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

    /**
     * {@link #resetDataManager() Resets the data manager} (which all redefinitions must do) and then
     * fades this activity.
     */
    protected boolean onReset() {
        resetDataManager();
        fadeActivity(LoginActivity.class, true);
        return true;
    }

    protected void resetDataManager() {
        DataManager dataManager = (DataManager) DataManager.create(this);
        dataManager.resetAll();
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
