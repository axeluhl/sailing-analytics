package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PreferencesBasedDeviceConfiguration extends DeviceConfigurationImpl implements ConfigurationLoader<DeviceConfiguration> {

    private static final long serialVersionUID = 9146162601389924219L;
    private static final String TAG = PreferencesBasedDeviceConfiguration.class.getSimpleName();

    private final AppPreferences preferences;
    private final ConfigurationLoader<RacingProceduresConfiguration> storableRacingProceduresConfiguration;
    
    public PreferencesBasedDeviceConfiguration(final AppPreferences preferences, 
            PreferencesBasedRacingProceduresConfiguration proceduresConfiguration) {
        super(proceduresConfiguration);
        this.preferences = preferences;
        this.storableRacingProceduresConfiguration = proceduresConfiguration;
    }

    @Override
    public DeviceConfiguration load() {
        if (storableRacingProceduresConfiguration != null) {
            setRacingProceduresConfiguration(storableRacingProceduresConfiguration.load());
        }
        
        setAllowedCourseAreaNames(preferences.getManagedCourseAreaNames());
        setResultsMailRecipient(preferences.getMailRecipient());
        
        if (preferences.isDefaultRacingProcedureTypeOverridden()) {
            setDefaultRacingProcedureType(preferences.getDefaultRacingProcedureType());
        }
        if (preferences.isDefaultCourseDesignerModeOverridden()) {
            setDefaultCourseDesignerMode(preferences.getDefaultCourseDesignerMode());
        }
        setByNameDesignerCourseNames(preferences.getByNameCourseDesignerCourseNames());
        return copy();
    }

    @Override
    public void store() {
        ExLog.i(TAG, "Storing new device configuration.");
        
        if (storableRacingProceduresConfiguration != null) {
            storableRacingProceduresConfiguration.store();
            preferences.setRacingProcedureConfigurationOverwriteAllowed(false);
        } else {
            preferences.setRacingProcedureConfigurationOverwriteAllowed(true);
        }
        
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
    }
    
    private static void logApply(String configurationName, Object value) {
        ExLog.i(TAG, String.format("Applied '%s' configuration: %s.", configurationName, value.toString()));
    }

}
