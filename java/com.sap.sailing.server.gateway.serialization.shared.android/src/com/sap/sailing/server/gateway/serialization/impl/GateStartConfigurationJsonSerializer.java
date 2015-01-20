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
    
    public static final String FIELD_HAS_PATHFINDER = "hasPathfinder";
    public static final String FIELD_HAS_ADDITIONAL_GOLF_DOWN_TIME = "hasAdditionalGolfDownTime";

    public GateStartConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        GateStartConfiguration object = (GateStartConfiguration) baseObject;
        
        if (object.hasPathfinder() != null) {
            result.put(FIELD_HAS_PATHFINDER, object.hasPathfinder());
        }
        
        if (object.hasAdditionalGolfDownTime() != null) {
            result.put(FIELD_HAS_ADDITIONAL_GOLF_DOWN_TIME, object.hasAdditionalGolfDownTime());
        }

        return result;
    }

}