package com.sap.sailing.racecommittee.app;

import com.sap.sailing.android.shared.application.LoggableApplication;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;

import io.branch.referral.Branch;

public class RaceApplication extends LoggableApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        new PreferenceHelper(this).setupPreferences();
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);
    }
}
