package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class GPSFixJsonSerializer implements JsonSerializer<GPSFix> {
    @Override
    public JSONObject serialize(GPSFix object) {
        JSONObject result = new JSONObject();

        result.put(TypeBasedJsonDeserializer.FIELD_TYPE, GPSFixJsonDeserializer.TYPE);
        result.put(GPSFixJsonDeserializer.FIELD_LAT_DEG, object.getPosition().getLatDeg());
        result.put(GPSFixJsonDeserializer.FIELD_LON_DEG, object.getPosition().getLngDeg());
        result.put(GPSFixJsonDeserializer.FIELD_TIME, object.getTimePoint().asMillis());

        return result;
    }
}
