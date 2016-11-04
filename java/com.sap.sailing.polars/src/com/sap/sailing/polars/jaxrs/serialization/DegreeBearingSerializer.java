package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DegreeBearingSerializer implements JsonSerializer<Bearing> {

    public static final String FIELD_DEGREES = "degrees";

    @Override
    public JSONObject serialize(Bearing object) {
        JSONObject bearingJSON = new JSONObject();
        bearingJSON.put(FIELD_DEGREES, object.getDegrees());
        return bearingJSON;
    }

}
