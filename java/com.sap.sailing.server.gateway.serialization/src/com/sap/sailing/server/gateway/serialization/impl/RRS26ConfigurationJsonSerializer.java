package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RRS26ConfigurationJsonSerializer extends RacingProcedureConfigurationJsonSerializer implements
        JsonSerializer<RacingProcedureConfiguration> {

    public static RRS26ConfigurationJsonSerializer create() {
        return new RRS26ConfigurationJsonSerializer();
    }

    public static final String FIELD_START_MODE_FLAGS = "startModeFlags";

    public RRS26ConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        RRS26Configuration object = (RRS26Configuration) baseObject;

        if (object.getStartModeFlags() != null) {
            JSONArray startModeFlags = new JSONArray();
            for (Flags item : object.getStartModeFlags()) {
                startModeFlags.add(item);
            }
            result.put(FIELD_START_MODE_FLAGS, startModeFlags);
        }

        return result;
    }

}