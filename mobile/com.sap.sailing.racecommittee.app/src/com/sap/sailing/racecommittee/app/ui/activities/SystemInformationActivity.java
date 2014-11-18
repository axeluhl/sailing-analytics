package com.sap.sailing.racecommittee.app.ui.activities;

import com.sap.sailing.racecommittee.app.R;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class SystemInformationActivity extends BaseActivity {
    private SystemInformationActivityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new SystemInformationActivityHelper(this, preferences.getDeviceIdentifier());
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }
    
    @Override
    protected void updateSendingServiceInformation() {
        helper.updateSendingServiceInformation();
        super.updateSendingServiceInformation();
    }
}
