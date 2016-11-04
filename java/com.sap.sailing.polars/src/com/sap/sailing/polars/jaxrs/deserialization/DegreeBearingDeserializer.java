package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.polars.jaxrs.serialization.DegreeBearingSerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class DegreeBearingDeserializer implements JsonDeserializer<Bearing> {

    @Override
    public Bearing deserialize(JSONObject object) throws JsonDeserializationException {
        return new DegreeBearingImpl((double) object.get(DegreeBearingSerializer.FIELD_DEGREES));
    }

}
