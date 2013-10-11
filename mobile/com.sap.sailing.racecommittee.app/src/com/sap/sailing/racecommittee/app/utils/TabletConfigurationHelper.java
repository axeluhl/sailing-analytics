package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.racecommittee.app.AppPreferences;

public class TabletConfigurationHelper {
    
    public static void apply(Context context, TabletConfiguration configuration) {
        if (configuration.getAllowedCourseAreaNames() != null) {
            AppPreferences.setManagedCourseAreaNames(context, configuration.getAllowedCourseAreaNames());
        }
        if (configuration.getMinimumRoundsForCourse() != null) {
            AppPreferences.setMinRounds(context, configuration.getMinimumRoundsForCourse());
        }
        if (configuration.getMaximumRoundsForCourse() != null) {
            AppPreferences.setMaxRounds(context, configuration.getMaximumRoundsForCourse());
        }
        if (configuration.getResultsMailRecipient() != null) {
            AppPreferences.setMailRecipient(context, configuration.getResultsMailRecipient());
        }
    }

}
