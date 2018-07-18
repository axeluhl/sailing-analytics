package com.sap.sailing.android.tracking.app.application;

import com.sap.sailing.android.shared.BuildConfig;
import com.sap.sailing.android.shared.application.LoggableApplication;

import io.branch.referral.Branch;

public class BranchApplication extends LoggableApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Branch.enableLogging();
        }
        Branch.getAutoInstance(this);
    }

}
