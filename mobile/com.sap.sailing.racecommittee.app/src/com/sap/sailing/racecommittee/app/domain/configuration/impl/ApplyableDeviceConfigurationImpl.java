package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import android.content.Context;

import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.ApplyableDeviceConfiguration;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class ApplyableDeviceConfigurationImpl extends DeviceConfigurationImpl implements ApplyableDeviceConfiguration {

    private static final long serialVersionUID = 9146162601389924219L;
    private static final String TAG = ApplyableDeviceConfigurationImpl.class.getName();

    @Override
    public void apply(Context context) {
        if (getAllowedCourseAreaNames() != null) {
            AppPreferences.setManagedCourseAreaNames(context, getAllowedCourseAreaNames());
            logApply("course areas", getAllowedCourseAreaNames());
        }
        if (getMinimumRoundsForCourse() != null) {
            AppPreferences.setMinRounds(context, getMinimumRoundsForCourse());
            logApply("minimum rounds", getMinimumRoundsForCourse());
        }
        if (getMaximumRoundsForCourse() != null) {
            AppPreferences.setMaxRounds(context, getMaximumRoundsForCourse());
            logApply("maximum rounds", getMaximumRoundsForCourse());
        }
        if (getResultsMailRecipient() != null) {
            AppPreferences.setMailRecipient(context, getResultsMailRecipient());
            logApply("mail recipient", getResultsMailRecipient());
        }
    }
    
    private static void logApply(String configurationName, Object value) {
        ExLog.i(TAG, String.format("Applied '%s' configuration: %s", configurationName, value.toString()));
    }

}
