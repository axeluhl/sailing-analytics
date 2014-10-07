package com.sap.sailing.android.tracking.app.ui.activities;

import android.os.Bundle;

import com.sap.sailing.android.shared.ui.activities.SystemInformationActivityHelper;
import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.android.tracking.app.R;

public class SystemInformationActivity extends BaseActivity {
    private SystemInformationActivityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new SystemInformationActivityHelper(this, PrefUtils.getString(this,
                R.string.preference_device_identifier_key, R.string.preference_device_identifier_default));
    }
    
    @Override
    protected void updateSendingServiceInformation() {
        helper.updateSendingServiceInformation();
        super.updateSendingServiceInformation();
    }

}
