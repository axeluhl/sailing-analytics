package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class GateJsonSerializer extends BaseControlPointJsonSerializer implements JsonSerializer<ControlPoint> {
    public static final String VALUE_CLASS = ControlPointWithTwoMarks.class.getSimpleName();

    public static final String FIELD_LEFT = "left";
    public static final String FIELD_RIGHT = "right";
    public static final String FIELD_ID = "id";

    private final JsonSerializer<ControlPoint> markSerializer;

    public GateJsonSerializer(JsonSerializer<ControlPoint> markSerializer) {
        this.markSerializer = markSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(ControlPoint object) {
        ControlPointWithTwoMarks gate = (ControlPointWithTwoMarks) object;
        JSONObject result =  super.serialize(gate);
        
        result.put(FIELD_ID, object.getId().toString());
        result.put(FIELD_LEFT, markSerializer.serialize(gate.getLeft()));
        result.put(FIELD_RIGHT, markSerializer.serialize(gate.getRight()));

        return result;
    }

}
