package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.BaseControlPointJsonSerializer;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

/**
 * Deserializer for controlpoints.
 */
public class ControlPointDeserializer implements JsonDeserializer<ControlPoint> {
    private final MarkDeserializer markDeserializer;
    private final GateDeserializer gateDeserializer;

    public ControlPointDeserializer(MarkDeserializer markDeserializer, GateDeserializer gateDeserializer) {
        this.markDeserializer = markDeserializer;
        this.gateDeserializer = gateDeserializer;
    }

    @Override
    public ControlPoint deserialize(JSONObject object) throws JsonDeserializationException {
        String classFieldValue = (String) object.get(BaseControlPointJsonSerializer.FIELD_CLASS);
        ControlPoint controlPoint = null;
        if (classFieldValue.equals(ControlPointWithTwoMarks.class.getSimpleName())) {
            controlPoint = gateDeserializer.deserialize(object);
        } else if (classFieldValue.equals(Mark.class.getSimpleName())) {
            controlPoint = markDeserializer.deserialize(object);
        }
        return controlPoint;
    }
}