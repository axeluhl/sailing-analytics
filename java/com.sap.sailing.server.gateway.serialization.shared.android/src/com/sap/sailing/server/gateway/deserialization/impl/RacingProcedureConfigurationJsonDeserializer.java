package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RacingProcedureConfigurationJsonSerializer;

public class RacingProcedureConfigurationJsonDeserializer implements JsonDeserializer<RacingProcedureConfiguration> {

    public static RacingProcedureConfigurationJsonDeserializer create() {
        return new RacingProcedureConfigurationJsonDeserializer();
    }

    public RacingProcedureConfigurationJsonDeserializer() {
    }

    @Override
    public RacingProcedureConfiguration deserialize(JSONObject object) throws JsonDeserializationException {

        Boolean inidividualRecall = null;
        if (object.containsKey(RacingProcedureConfigurationJsonSerializer.FIELD_INIDIVIDUAL_RECALL)) {
            inidividualRecall = ((Boolean) object
                    .get(RacingProcedureConfigurationJsonSerializer.FIELD_INIDIVIDUAL_RECALL));
        }

        Flags classFlag = null;
        if (object.containsKey(RacingProcedureConfigurationJsonSerializer.FIELD_CLASS_FLAG)) {
            classFlag = Flags.valueOf(object.get(RacingProcedureConfigurationJsonSerializer.FIELD_CLASS_FLAG)
                    .toString());
        }

        return createResult(object, inidividualRecall, classFlag);
    }

    protected RacingProcedureConfiguration createResult(JSONObject object, Boolean inidividualRecall, Flags classFlag) 
            throws JsonDeserializationException{
        RacingProcedureConfigurationImpl result = new RacingProcedureConfigurationImpl();
        result.setHasInidividualRecall(inidividualRecall);
        result.setClassFlag(classFlag);
        return result;
    }

}