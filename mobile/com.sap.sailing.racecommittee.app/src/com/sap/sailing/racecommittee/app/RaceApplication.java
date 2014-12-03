package com.sap.sailing.racecommittee.app;

import com.sap.sailing.android.shared.application.LoggableApplication;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;

public class RaceApplication extends LoggableApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        new PreferenceHelper(this).setupPreferences();
    }
}
