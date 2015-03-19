package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.shared.ui.activities.AbstractBaseActivity;

public class BaseActivity extends AbstractBaseActivity {
	
    protected AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.options_menu, menu);
        return true;
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
