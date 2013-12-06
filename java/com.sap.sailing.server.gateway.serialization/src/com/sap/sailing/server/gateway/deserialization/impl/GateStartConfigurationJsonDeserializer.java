package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.GateStartConfigurationJsonSerializer;


public class GateStartConfigurationJsonDeserializer extends RacingProcedureConfigurationJsonDeserializer implements JsonDeserializer<RacingProcedureConfiguration> {

    public static GateStartConfigurationJsonDeserializer create() {
        return new GateStartConfigurationJsonDeserializer();
    }


    public GateStartConfigurationJsonDeserializer() {
    	super();
    }

    @Override
    public GateStartConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
    	return (GateStartConfiguration) super.deserialize(object);    
    }
    
    @Override
    protected GateStartConfiguration createResult(JSONObject object, Boolean inidividualRecall, Flags classFlag) throws JsonDeserializationException {
    	GateStartConfigurationImpl result = new GateStartConfigurationImpl();
    	result.setClassFlag(classFlag);
    	result.setHasInidividualRecall(inidividualRecall);
    	
    	if (object.containsKey(GateStartConfigurationJsonSerializer.FIELD_HAS_PATHFINDER)) {
    	    result.setHasInidividualRecall((Boolean)object.get(GateStartConfigurationJsonSerializer.FIELD_HAS_PATHFINDER));
    	}
    	
        if (object.containsKey(GateStartConfigurationJsonSerializer.FIELD_HAS_ADDITIONAL_GOLF_DOWN_TIME)) {
            result.setHasAdditionalGolfDownTime((Boolean)object.get(GateStartConfigurationJsonSerializer.FIELD_HAS_ADDITIONAL_GOLF_DOWN_TIME));
        }
    	return result;
    }

}