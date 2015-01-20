package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.PositionJsonSerializer;

public class PositionJsonDeserializer implements JsonDeserializer<Position> {

    public Position deserialize(JSONObject object) throws JsonDeserializationException {
        Number latitudeDeg = (Number) object.get(PositionJsonSerializer.FIELD_LATITUDE_DEG);
        Number longitudeDeg = (Number) object.get(PositionJsonSerializer.FIELD_LONGITUDE_DEG);
        return new DegreePosition(latitudeDeg.doubleValue(), longitudeDeg.doubleValue());
    }

}