package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.AbstractStartActivity;
import com.sap.sailing.android.shared.ui.dialogs.AboutDialog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;

public class StartActivity extends AbstractStartActivity {

    private AppPreferences prefs;
    private final String TAG = StartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_activity_start));
            getSupportActionBar().setHomeButtonEnabled(false);
        }
        replaceFragment(R.id.content_frame, new HomeFragment());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (prefs.getTrackerIsTracking()) {
            String checkinDigest = prefs.getTrackerIsTrackingCheckinDigest();
            startRegatta(checkinDigest);
        }
    }

    @Override
    public AbstractHomeFragment getHomeFragment() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        return homeFragment;
    }

    //	/**
    //     * Hockeyapp integration method.
    //     */
    //	private void checkForUpdates() {
    //		// TODO: Remove this for store builds!
    //		UpdateManager.register(this, "060ff0c8a907638e3b31d3146091c87b");
    //	}

    private void startRegatta(String checkinDigest) {
        Intent intent = new Intent(this, RegattaActivity.class);
        intent.putExtra(getString(R.string.checkin_digest), checkinDigest);
        startActivity(intent);
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
