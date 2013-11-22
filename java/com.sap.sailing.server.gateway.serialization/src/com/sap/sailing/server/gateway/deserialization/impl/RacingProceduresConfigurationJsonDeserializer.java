package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RacingProceduresConfigurationJsonSerializer;

public class RacingProceduresConfigurationJsonDeserializer implements JsonDeserializer<RacingProceduresConfiguration> {

    @Override
    public RacingProceduresConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        RacingProceduresConfigurationImpl configuration = createConfiguration();
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_HAS_XRAY)) {
            Boolean hasIndividualRecall = (Boolean) object.get(RacingProceduresConfigurationJsonSerializer.FIELD_HAS_XRAY);
            configuration.setHasInidividualRecall(hasIndividualRecall);
        }
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_CLASS_FLAG)) {
            Flags classFlag = Flags.valueOf(object.get(RacingProceduresConfigurationJsonSerializer.FIELD_CLASS_FLAG).toString());
            configuration.setClassFlag(classFlag);
        }
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_START_MODE_FLAGS)) {
            List<Flags> result = new ArrayList<Flags>();
            JSONArray startModeFlags = Helpers.getNestedArraySafe(object, 
                    RacingProceduresConfigurationJsonSerializer.FIELD_START_MODE_FLAGS);
            for (Object startModeFlag : startModeFlags) {
                result.add(Flags.valueOf(startModeFlag.toString()));
            }
            configuration.setStartModeFlags(result);
        }
        
        return configuration;
    }
    
    protected RacingProceduresConfigurationImpl createConfiguration() {
        return new RacingProceduresConfigurationImpl();
    }

}
