package com.sap.sailing.racecommittee.app.data.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesBasedRegattaConfiguration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.ESSConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GateStartConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RRS26ConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RacingProcedureConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RegattaConfigurationJsonDeserializer;

public class PreferencesBasedRegattaConfigurationJsonDeserializer extends RegattaConfigurationJsonDeserializer {
    
    private final AppPreferences preferences;
    
    public static PreferencesBasedRegattaConfigurationJsonDeserializer create(AppPreferences preferences) {
        return new PreferencesBasedRegattaConfigurationJsonDeserializer(preferences, RRS26ConfigurationJsonDeserializer.create(),
                GateStartConfigurationJsonDeserializer.create(), ESSConfigurationJsonDeserializer.create(),
                RacingProcedureConfigurationJsonDeserializer.create());
    }

    public PreferencesBasedRegattaConfigurationJsonDeserializer(AppPreferences preferences, 
            RRS26ConfigurationJsonDeserializer rrs26, GateStartConfigurationJsonDeserializer gateStart, 
            ESSConfigurationJsonDeserializer ess, RacingProcedureConfigurationJsonDeserializer basic) {
        super(rrs26, gateStart, ess, basic);
        this.preferences = preferences;
    }
    
    @Override
    protected RegattaConfigurationImpl createConfiguration() {
        return new PreferencesBasedRegattaConfiguration(preferences);
    }
    
    public PreferencesBasedRegattaConfiguration deserializeAsStored(JSONObject object) throws JsonDeserializationException {
        return (PreferencesBasedRegattaConfiguration) deserialize(object);
    }
}
