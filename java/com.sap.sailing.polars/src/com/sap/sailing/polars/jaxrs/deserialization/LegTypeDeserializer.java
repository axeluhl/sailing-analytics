package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.polars.jaxrs.serialization.LegTypeSerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class LegTypeDeserializer implements JsonDeserializer<LegType> {

    @Override
    public LegType deserialize(JSONObject object) throws JsonDeserializationException {
        return LegType.valueOf((String) object.get(LegTypeSerializer.FIELD_VALUE));
    }

}
