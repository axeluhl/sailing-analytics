package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class GateStartConfigurationJsonSerializer extends RacingProcedureConfigurationJsonSerializer implements
        JsonSerializer<RacingProcedureConfiguration> {

    public static GateStartConfigurationJsonSerializer create() {
        return new GateStartConfigurationJsonSerializer();
    }

    public GateStartConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        @SuppressWarnings("unused")
        GateStartConfiguration object = (GateStartConfiguration) baseObject;

        return result;
    }

}