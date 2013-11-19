package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.StoredDeviceConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PreferencesBasedDeviceConfiguration extends DeviceConfigurationImpl implements StoredDeviceConfiguration {

    private static final long serialVersionUID = 9146162601389924219L;
    private static final String TAG = PreferencesBasedDeviceConfiguration.class.getName();

    private final AppPreferences preferences;
    
    public PreferencesBasedDeviceConfiguration(final AppPreferences preferences) {
        super(new PreferencesBasedRacingProceduresConfiguration(preferences).load());
        this.preferences = preferences;
    }
    
    public PreferencesBasedDeviceConfiguration(final AppPreferences preferences, StoredRacingProceduresConfiguration proceduresConfiguration) {
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
        
        setAllowedCourseAreaNames(preferences.getManagedCourseAreaNames());
        setResultsMailRecipient(preferences.getMailRecipient());
        
        if (preferences.isDefaultRacingProcedureTypeOverridden()) {
            setDefaultRacingProcedureType(preferences.getDefaultRacingProcedureType());
        }
        if (preferences.isDefaultCourseDesignerModeOverridden()) {
            setDefaultCourseDesignerMode(preferences.getDefaultCourseDesignerMode());
        }
        setByNameDesignerCourseNames(preferences.getByNameCourseDesignerCourseNames());
        return this;
    }
    
    @Override
    public StoredDeviceConfiguration store() {
        getRacingProceduresConfiguration().store();
        
        if (getAllowedCourseAreaNames() != null) {
            preferences.setManagedCourseAreaNames(getAllowedCourseAreaNames());
            logApply("course areas", getAllowedCourseAreaNames());
        }
        if (getResultsMailRecipient() != null) {
            preferences.setMailRecipient(getResultsMailRecipient());
            logApply("mail recipient", getResultsMailRecipient());
        }
        if (getDefaultRacingProcedureType() != null) {
            preferences.setDefaultRacingProcedureTypeOverridden(true);
            preferences.setDefaultRacingProcedureType(getDefaultRacingProcedureType());
            logApply("overridden racing procedure", getDefaultRacingProcedureType());
        } else {
            preferences.setDefaultRacingProcedureTypeOverridden(false);
        }
        if (getDefaultCourseDesignerMode() != null) {
            preferences.setDefaultCourseDesignerModeOverridden(true);
            preferences.setDefaultCourseDesignerMode(getDefaultCourseDesignerMode());
            logApply("overridden course designer mode", getDefaultCourseDesignerMode());
        } else {
            preferences.setDefaultCourseDesignerModeOverridden(false);
        }
        if (getByNameCourseDesignerCourseNames() != null) {
            preferences.setByNameCourseDesignerCourseNames(getByNameCourseDesignerCourseNames());
            logApply("by name course designer course names", getByNameCourseDesignerCourseNames());
        }
        return this;
    }
    
    private static void logApply(String configurationName, Object value) {
        ExLog.i(TAG, String.format("Applied '%s' configuration: %s", configurationName, value.toString()));
    }

}
