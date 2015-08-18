package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.ui.activities.AbstractStartActivity;
import com.sap.sailing.android.shared.ui.dialogs.AboutDialog;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;

public class StartActivity extends AbstractStartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_activity_start));
            getSupportActionBar().setHomeButtonEnabled(false);
        }
        replaceFragment(R.id.content_frame, new HomeFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            AboutDialog aboutDialog = new AboutDialog(this);
            aboutDialog.show();
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getOptionsMenuResId() {
        return 0;
    }

    public AbstractHomeFragment getHomeFragment() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        return homeFragment;
    }

    @Override
    public void onCheckinDataAvailable(AbstractCheckinData data) {
        if (data != null) {
            getHomeFragment().displayUserConfirmationScreen(data);
        }
    }
}
