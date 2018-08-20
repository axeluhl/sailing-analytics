package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class WaypointJsonSerializer implements JsonSerializer<Waypoint> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PASSING_INSTRUCTIONS = "passingInstruction";
    public static final String FIELD_CONTROL_POINT = "controlPoint";

    private final JsonSerializer<ControlPoint> controlPointSerializer;

    public WaypointJsonSerializer(JsonSerializer<ControlPoint> controlPointSerializer) {
        this.controlPointSerializer = controlPointSerializer;
    }

    @Override
    public JSONObject serialize(Waypoint object) {
        JSONObject result = new JSONObject();
        
        if (object.getName() != null) {
            result.put(FIELD_NAME, object.getName());
        }

        if (object.getPassingInstructions() != null) {
            result.put(FIELD_PASSING_INSTRUCTIONS, object.getPassingInstructions().name());
        }
        result.put(FIELD_CONTROL_POINT, 
                controlPointSerializer.serialize(object.getControlPoint()));

        return result;
    }

}
