package com.sap.sailing.racecommittee.app;

import android.content.Intent;

import com.sap.sailing.android.shared.application.LoggableApplication;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;

public class RaceApplication extends LoggableApplication {

    private static RaceApplication sInstance;

    public static RaceApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        new PreferenceHelper(this).setupPreferences();
    }

    public void restart() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
