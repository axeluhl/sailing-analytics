package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RacingProceduresConfigurationJsonSerializer implements JsonSerializer<RacingProceduresConfiguration> {

    @Override
    public JSONObject serialize(RacingProceduresConfiguration object) {
        JSONObject result = new JSONObject();
        return result;
    }

}
