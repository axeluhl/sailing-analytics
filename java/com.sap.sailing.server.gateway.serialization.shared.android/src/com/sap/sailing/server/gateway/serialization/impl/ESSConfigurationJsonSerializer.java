package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class ESSConfigurationJsonSerializer extends RacingProcedureConfigurationJsonSerializer implements
        JsonSerializer<RacingProcedureConfiguration> {

    public static ESSConfigurationJsonSerializer create() {
        return new ESSConfigurationJsonSerializer();
    }

    public ESSConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        @SuppressWarnings("unused")
        ESSConfiguration object = (ESSConfiguration) baseObject;

        return result;
    }

}