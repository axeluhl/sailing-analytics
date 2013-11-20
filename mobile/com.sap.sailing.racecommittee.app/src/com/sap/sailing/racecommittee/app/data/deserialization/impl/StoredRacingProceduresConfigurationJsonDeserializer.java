package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
import com.sap.sailing.server.gateway.deserialization.impl.RacingProceduresConfigurationJsonDeserializer;

public class StoredRacingProceduresConfigurationJsonDeserializer extends RacingProceduresConfigurationJsonDeserializer {
    
    private final AppPreferences preferences;

    public StoredRacingProceduresConfigurationJsonDeserializer(AppPreferences preferences) {
        this.preferences = preferences;
    }
    
    @Override
    protected RacingProceduresConfigurationImpl createConfiguration() {
        return new PreferencesBasedRacingProceduresConfiguration(preferences);
    }
}
