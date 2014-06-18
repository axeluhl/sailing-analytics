package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RRS26ConfigurationJsonSerializer;

public class RRS26ConfigurationJsonDeserializer extends RacingProcedureConfigurationJsonDeserializer implements
        JsonDeserializer<RacingProcedureConfiguration> {

    public static RRS26ConfigurationJsonDeserializer create() {
        return new RRS26ConfigurationJsonDeserializer();
    }

    public RRS26ConfigurationJsonDeserializer() {
        super();
    }

    @Override
    public RRS26Configuration deserialize(JSONObject object) throws JsonDeserializationException {
        return (RRS26Configuration) super.deserialize(object);
    }
    
    @Override
    protected RacingProcedureConfiguration createResult(JSONObject object, Boolean inidividualRecall, Flags classFlag)
            throws JsonDeserializationException {
        List<Flags> startModeFlags = null;
        if (object.containsKey(RRS26ConfigurationJsonSerializer.FIELD_START_MODE_FLAGS)) {
            startModeFlags = new ArrayList<Flags>();
            JSONArray objects = Helpers.getNestedArraySafe(object,
                    RRS26ConfigurationJsonSerializer.FIELD_START_MODE_FLAGS);
            for (Object value : objects) {
                startModeFlags.add(Flags.valueOf(value.toString()));
            }
        }

        RRS26ConfigurationImpl result = new RRS26ConfigurationImpl();
        result.setClassFlag(classFlag);
        result.setHasInidividualRecall(inidividualRecall);
        result.setStartModeFlags(startModeFlags);
        return result;
    }

}