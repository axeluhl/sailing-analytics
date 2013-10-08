package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.racecommittee.app.AppPreferences;

public class TabletConfigurationHelper {
    
    public static void apply(Context context, TabletConfiguration configuration) {
        AppPreferences.setManagedCourseAreaNames(context, configuration.getAllowedCourseAreaNames());
    }

}
