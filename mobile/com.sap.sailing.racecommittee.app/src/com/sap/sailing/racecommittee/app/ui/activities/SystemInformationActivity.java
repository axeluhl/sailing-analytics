package com.sap.sailing.racecommittee.app.ui.activities;

import android.os.Bundle;

import com.sap.sailing.android.shared.ui.activities.SystemInformationActivityHelper;

public class SystemInformationActivity extends BaseActivity {
    private SystemInformationActivityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new SystemInformationActivityHelper(this, preferences.getDeviceIdentifier());
    }
    
    @Override
    protected void updateSendingServiceInformation() {
        helper.updateSendingServiceInformation();
        super.updateSendingServiceInformation();
    }

}
