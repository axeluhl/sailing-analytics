package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RacingProceduresConfigurationJsonSerializer implements JsonSerializer<RacingProceduresConfiguration> {

    public static final String FIELD_CLASS_FLAG = "classFlag";
    public static final String FIELD_START_MODE_FLAGS = "startModeFlags";
    public static final String FIELD_HAS_XRAY = "hasIndividualRecall";
    
    @Override
    public JSONObject serialize(RacingProceduresConfiguration object) {
        JSONObject result = new JSONObject();
        if (object.hasInidividualRecall() != null) {
            result.put(FIELD_HAS_XRAY, object.hasInidividualRecall());
        }
        
        if (object.getClassFlag() != null) {
            result.put(FIELD_CLASS_FLAG, object.getClassFlag().name());
        }
        if (object.getStartModeFlags() != null) {
            JSONArray startModeFlags = new JSONArray();
            for (Flags startModeFlag : object.getStartModeFlags()) {
                startModeFlags.add(startModeFlag);
            }
            result.put(FIELD_START_MODE_FLAGS, startModeFlags);
        }
        return result;
    }

}
