package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class TabletConfigurationHelper {
    
    private static final String TAG = TabletConfigurationHelper.class.getName();

    public static void apply(Context context, TabletConfiguration configuration) {
        if (configuration.getAllowedCourseAreaNames() != null) {
            AppPreferences.setManagedCourseAreaNames(context, configuration.getAllowedCourseAreaNames());
            logApply("course areas", configuration.getAllowedCourseAreaNames());
        }
        if (configuration.getMinimumRoundsForCourse() != null) {
            AppPreferences.setMinRounds(context, configuration.getMinimumRoundsForCourse());
            logApply("minimum rounds", configuration.getMinimumRoundsForCourse());
        }
        if (configuration.getMaximumRoundsForCourse() != null) {
            AppPreferences.setMaxRounds(context, configuration.getMaximumRoundsForCourse());
            logApply("maximum rounds", configuration.getMaximumRoundsForCourse());
        }
        if (configuration.getResultsMailRecipient() != null) {
            AppPreferences.setMailRecipient(context, configuration.getResultsMailRecipient());
            logApply("mail recipient", configuration.getResultsMailRecipient());
        }
    }
    
    private static void logApply(String configurationName, Object value) {
        ExLog.i(TAG, String.format("Applied %s configuration: %s", configurationName, value.toString()));
    }

}
