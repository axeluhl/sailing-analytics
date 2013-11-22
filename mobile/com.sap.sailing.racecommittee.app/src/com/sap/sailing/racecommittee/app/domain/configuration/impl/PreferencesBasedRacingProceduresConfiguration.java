package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PreferencesBasedRacingProceduresConfiguration extends RacingProceduresConfigurationImpl implements
    ConfigurationLoader<RacingProceduresConfiguration> {

    private static final long serialVersionUID = -2109422929668306199L;
    private static final String TAG = PreferencesBasedRacingProceduresConfiguration.class.getName();

    private final AppPreferences preferences;

    public PreferencesBasedRacingProceduresConfiguration(final AppPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public RacingProceduresConfiguration load() {
        // TODO: implement loading of RPC
        setClassFlag(null);
        setHasInidividualRecall(null);
        
        setStartModeFlags(null);
        return copy();
    }

    @Override
    public void store() {
        if (!preferences.isRacingProcedureConfigurationOverwriteAllowed()) {
            ExLog.i(TAG, "Overwrite of procedure configuration not allowed.");
            return;
        }
        ExLog.i(TAG, "Storing new racing procedure configuration.");

        // TODO: implement storing of RPC
        if (getClassFlag() != null) {
            
        }

        if (hasInidividualRecall() != null) {
            
        }
        
        if (getStartModeFlags() != null) {
            
        }
        
    }

}
