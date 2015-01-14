package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class ESSConfigurationJsonDeserializer extends RacingProcedureConfigurationJsonDeserializer implements
        JsonDeserializer<RacingProcedureConfiguration> {

    public static ESSConfigurationJsonDeserializer create() {
        return new ESSConfigurationJsonDeserializer();
    }

    public ESSConfigurationJsonDeserializer() {
        super();
    }

    @Override
    public ESSConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        return (ESSConfiguration) super.deserialize(object);
    }

    @Override
    protected ESSConfiguration createResult(JSONObject object, Boolean inidividualRecall, Flags classFlag)
            throws JsonDeserializationException {
        ESSConfigurationImpl result = new ESSConfigurationImpl();
        result.setClassFlag(classFlag);
        result.setHasInidividualRecall(inidividualRecall);
        return result;
    }

}