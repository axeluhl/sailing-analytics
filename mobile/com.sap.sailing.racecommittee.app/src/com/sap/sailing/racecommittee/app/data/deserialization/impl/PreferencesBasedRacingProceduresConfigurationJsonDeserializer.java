package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRacingProceduresConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.ESSConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GateStartConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RRS26ConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RacingProceduresConfigurationJsonDeserializer;

public class PreferencesBasedRacingProceduresConfigurationJsonDeserializer extends RacingProceduresConfigurationJsonDeserializer {
    
    private final AppPreferences preferences;
    
    public static PreferencesBasedRacingProceduresConfigurationJsonDeserializer create(AppPreferences preferences) {
        return new PreferencesBasedRacingProceduresConfigurationJsonDeserializer(preferences, RRS26ConfigurationJsonDeserializer.create(),
                GateStartConfigurationJsonDeserializer.create(), ESSConfigurationJsonDeserializer.create());
    }

    public PreferencesBasedRacingProceduresConfigurationJsonDeserializer(AppPreferences preferences, 
            RRS26ConfigurationJsonDeserializer rrs26, GateStartConfigurationJsonDeserializer gateStart, 
            ESSConfigurationJsonDeserializer ess) {
        super(rrs26, gateStart, ess);
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
