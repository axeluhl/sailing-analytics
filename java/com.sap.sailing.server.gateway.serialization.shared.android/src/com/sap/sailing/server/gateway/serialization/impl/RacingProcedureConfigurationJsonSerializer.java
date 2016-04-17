package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RacingProcedureConfigurationJsonSerializer implements JsonSerializer<RacingProcedureConfiguration> {

    public static RacingProcedureConfigurationJsonSerializer create() {
        return new RacingProcedureConfigurationJsonSerializer();
    }

    public static final String FIELD_INIDIVIDUAL_RECALL = "individualRecall";
    public static final String FIELD_RESULT_ENTRY_ENABLED = "resultEntryEnabled";
    public static final String FIELD_CLASS_FLAG = "classFlag";

    public RacingProcedureConfigurationJsonSerializer() {
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration object) {
        JSONObject result = new JSONObject();
        if (object.hasIndividualRecall() != null) {
            result.put(FIELD_INIDIVIDUAL_RECALL, object.hasIndividualRecall());
        }
        if (object.isResultEntryEnabled() != null) {
            result.put(FIELD_RESULT_ENTRY_ENABLED, object.isResultEntryEnabled());
        }
        if (object.getClassFlag() != null) {
            result.put(FIELD_CLASS_FLAG, object.getClassFlag().name());
        }

        return result;
    }

}