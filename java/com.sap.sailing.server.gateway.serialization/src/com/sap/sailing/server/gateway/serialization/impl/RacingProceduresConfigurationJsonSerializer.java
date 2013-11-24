package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RacingProceduresConfigurationJsonSerializer implements JsonSerializer<RacingProceduresConfiguration> {

    private JsonSerializer<RacingProcedureConfiguration> rrs26Serializer;
    private JsonSerializer<RacingProcedureConfiguration> gateStartSerializer;
    private JsonSerializer<RacingProcedureConfiguration> essSerializer;

    public static RacingProceduresConfigurationJsonSerializer create() {
        return new RacingProceduresConfigurationJsonSerializer(RRS26ConfigurationJsonSerializer.create(),
                GateStartConfigurationJsonSerializer.create(), ESSConfigurationJsonSerializer.create());
    }

    public RacingProceduresConfigurationJsonSerializer(JsonSerializer<RacingProcedureConfiguration> rrs26,
            JsonSerializer<RacingProcedureConfiguration> gateStart, JsonSerializer<RacingProcedureConfiguration> ess) {
        this.rrs26Serializer = rrs26;
        this.gateStartSerializer = gateStart;
        this.essSerializer = ess;
    }

    public static final String FIELD_RRS26 = "rrs26";
    public static final String FIELD_GATE_START = "gateStart";
    public static final String FIELD_ESS = "ess";

    @Override
    public JSONObject serialize(RacingProceduresConfiguration object) {
        JSONObject result = new JSONObject();
        if (object.getRRS26Configuration() != null) {
            result.put(FIELD_RRS26, rrs26Serializer.serialize(object.getRRS26Configuration()));
        }

        if (object.getGateStartConfiguration() != null) {
            result.put(FIELD_GATE_START, gateStartSerializer.serialize(object.getGateStartConfiguration()));
        }
        if (object.getESSConfiguration() != null) {
            result.put(FIELD_ESS, essSerializer.serialize(object.getESSConfiguration()));
        }
        return result;
    }

}
