package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.impl.SWCStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.SWCStartConfigurationJsonSerializer;

public class SWCStartConfigurationJsonDeserializer extends RacingProcedureConfigurationJsonDeserializer implements
        JsonDeserializer<RacingProcedureConfiguration> {

    public static SWCStartConfigurationJsonDeserializer create() {
        return new SWCStartConfigurationJsonDeserializer();
    }

    public SWCStartConfigurationJsonDeserializer() {
        super();
    }

    @Override
    public SWCStartConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        return (SWCStartConfiguration) super.deserialize(object);
    }
    
    @Override
    protected RacingProcedureConfiguration createResult(JSONObject object, Boolean inidividualRecall, Boolean resultEntryEnabled, Flags classFlag)
            throws JsonDeserializationException {
        List<Flags> startModeFlags = null;
        if (object.containsKey(SWCStartConfigurationJsonSerializer.FIELD_START_MODE_FLAGS)) {
            startModeFlags = new ArrayList<Flags>();
            JSONArray objects = Helpers.getNestedArraySafe(object,
                    SWCStartConfigurationJsonSerializer.FIELD_START_MODE_FLAGS);
            for (Object value : objects) {
                startModeFlags.add(Flags.valueOf(value.toString()));
            }
        }
        SWCStartConfigurationImpl result = new SWCStartConfigurationImpl();
        result.setClassFlag(classFlag);
        result.setHasIndividualRecall(inidividualRecall);
        result.setResultEntryEnabled(resultEntryEnabled);
        result.setStartModeFlags(startModeFlags);
        return result;
    }

}