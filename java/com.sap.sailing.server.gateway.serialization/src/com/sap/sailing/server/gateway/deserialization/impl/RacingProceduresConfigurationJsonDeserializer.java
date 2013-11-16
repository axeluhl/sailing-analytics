package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class RacingProceduresConfigurationJsonDeserializer implements JsonDeserializer<RacingProceduresConfiguration> {

    @Override
    public RacingProceduresConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        RacingProceduresConfigurationImpl configuration = createConfiguration();
        return configuration;
    }
    
    protected RacingProceduresConfigurationImpl createConfiguration() {
        return new RacingProceduresConfigurationImpl();
    }

}
