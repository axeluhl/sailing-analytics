package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class ControlPointJsonSerializer implements JsonSerializer<ControlPoint> {

    private JsonSerializer<ControlPoint> markSerializer;
    private JsonSerializer<ControlPoint> gateSerializer;

    public ControlPointJsonSerializer(
            JsonSerializer<ControlPoint> markSerializer,
            JsonSerializer<ControlPoint> gateSerializer) {
        this.markSerializer = markSerializer;
        this.gateSerializer = gateSerializer;
    }

    protected JsonSerializer<ControlPoint> getSerializer(
            ControlPoint controlPoint) {
        if (controlPoint instanceof Mark) {
            return markSerializer;
        } else if (controlPoint instanceof ControlPointWithTwoMarks) {
            return gateSerializer;
        }

        throw new UnsupportedOperationException(String.format(
                "There is no serializer defined for control point type %s", controlPoint
                .getClass().getName()));
    }

    @Override
    public JSONObject serialize(ControlPoint object) {
        return getSerializer(object).serialize(object);
    }

}
