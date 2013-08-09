package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class WindJsonSerializer implements JsonSerializer<Wind> {
    public static final String FIELD_POSITION = "position";
    public static final String FIELD_TIMEPOINT = "timepoint";
    public static final String FIELD_SPEED_IN_KNOTS = "speedinknots";
    
    // direction the wind flows to (not from)
    public static final String FIELD_BEARING = "bearing";

    private final JsonSerializer<Position> positionSerializer;

    public WindJsonSerializer(JsonSerializer<Position> positionSerializer) {
        this.positionSerializer = positionSerializer;
    }

    @Override
    public JSONObject serialize(Wind wind) {
        JSONObject result = new JSONObject();

        result.put(FIELD_POSITION, positionSerializer.serialize(wind.getPosition()));
        result.put(FIELD_TIMEPOINT, wind.getTimePoint().asMillis());
        result.put(FIELD_SPEED_IN_KNOTS, wind.getKnots());
        result.put(FIELD_BEARING, wind.getBearing().getDegrees());

        return result;
    }
}
