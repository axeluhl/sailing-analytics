package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.shared.ui.activities.AbstractBaseActivity;

import android.os.Bundle;
import android.view.MenuItem;

public class BaseActivity extends AbstractBaseActivity {

    protected AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getOptionsMenuResId() {
        return 0;
    }
}
