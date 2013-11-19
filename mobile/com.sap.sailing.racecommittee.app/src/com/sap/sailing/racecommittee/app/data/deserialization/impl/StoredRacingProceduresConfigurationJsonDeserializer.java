package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
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
    
    @Override
    public StoredRacingProceduresConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        return (StoredRacingProceduresConfiguration) super.deserialize(object);
    }
}
