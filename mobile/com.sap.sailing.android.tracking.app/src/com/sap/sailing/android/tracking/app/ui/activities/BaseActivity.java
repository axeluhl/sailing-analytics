package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class BaseActivity extends ActionBarActivity {
    private static final String TAG = SendingServiceAwareActivity.class.getName();
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
            startActivity(new Intent(this, SystemInformationActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void replaceFragment(int view, Fragment fragment) {
        ExLog.i(this, TAG, "Set new Fragment: " + fragment.toString());
        
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(view, fragment);
        transaction.commit();
    }
}
