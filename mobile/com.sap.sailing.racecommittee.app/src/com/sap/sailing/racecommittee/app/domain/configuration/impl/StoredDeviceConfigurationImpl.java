package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.StoredDeviceConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class StoredDeviceConfigurationImpl extends DeviceConfigurationImpl implements StoredDeviceConfiguration {

    private static final long serialVersionUID = 9146162601389924219L;
    private static final String TAG = StoredDeviceConfigurationImpl.class.getName();

    private final AppPreferences preferences;
    
    public StoredDeviceConfigurationImpl(final AppPreferences preferences) {
        super(new StoredRacingProceduresConfigurationImpl(preferences).load());
        this.preferences = preferences;
    }
    
    public StoredDeviceConfigurationImpl(final AppPreferences preferences, StoredRacingProceduresConfiguration proceduresConfiguration) {
        super(proceduresConfiguration);
        this.preferences = preferences;
    }
    
    @Override
    public StoredRacingProceduresConfiguration getRacingProceduresConfiguration() {
        return (StoredRacingProceduresConfiguration) super.getRacingProceduresConfiguration();
    }

    @Override
    public StoredDeviceConfiguration load() {
        getRacingProceduresConfiguration().load();
        
        // TODO implement loading from AppPreferences
        return this;
    }
    
    @Override
    public StoredDeviceConfiguration store() {
        getRacingProceduresConfiguration().store();
        
        if (getAllowedCourseAreaNames() != null) {
            preferences.setManagedCourseAreaNames(getAllowedCourseAreaNames());
            logApply("course areas", getAllowedCourseAreaNames());
        }
        if (getMinimumRoundsForCourse() != null) {
            preferences.setMinRounds(getMinimumRoundsForCourse());
            logApply("minimum rounds", getMinimumRoundsForCourse());
        }
        if (getMaximumRoundsForCourse() != null) {
            preferences.setMaxRounds(getMaximumRoundsForCourse());
            logApply("maximum rounds", getMaximumRoundsForCourse());
        }
        if (getResultsMailRecipient() != null) {
            preferences.setMailRecipient(getResultsMailRecipient());
            logApply("mail recipient", getResultsMailRecipient());
        }
        return this;
    }
    
    private static void logApply(String configurationName, Object value) {
        ExLog.i(TAG, String.format("Applied '%s' configuration: %s", configurationName, value.toString()));
    }

}
