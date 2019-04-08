package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SWCStartConfigurationJsonSerializer extends RacingProcedureConfigurationJsonSerializer implements
        JsonSerializer<RacingProcedureConfiguration> {

    public static SWCStartConfigurationJsonSerializer create() {
        return new SWCStartConfigurationJsonSerializer();
    }

    public static final String FIELD_START_MODE_FLAGS = "startModeFlags";

    public SWCStartConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        SWCStartConfiguration object = (SWCStartConfiguration) baseObject;

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