package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.RacingProceduresConfigurationJsonDeserializer;

public class PreferencesBasedRacingProceduresConfigurationJsonDeserializer extends RacingProceduresConfigurationJsonDeserializer {
    
    private final AppPreferences preferences;

    public PreferencesBasedRacingProceduresConfigurationJsonDeserializer(AppPreferences preferences) {
        this.preferences = preferences;
    }
    
    @Override
    protected RacingProceduresConfigurationImpl createConfiguration() {
        return new PreferencesBasedRacingProceduresConfiguration(preferences);
    }
    
    public PreferencesBasedRacingProceduresConfiguration deserializeAsStored(JSONObject object) throws JsonDeserializationException {
        return (PreferencesBasedRacingProceduresConfiguration) deserialize(object);
    }
}
