package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;

/**
 * Deserializer for waypoints.
 */
public class WaypointDeserializer implements JsonDeserializer<Waypoint> {

    private final ControlPointDeserializer controlPointDeserializer;

    public WaypointDeserializer(ControlPointDeserializer controlPointDeserializer) {
        this.controlPointDeserializer = controlPointDeserializer;
    }

    @Override
    public Waypoint deserialize(JSONObject object) throws JsonDeserializationException {
        Object passingInstructionsObject = object.get(WaypointJsonSerializer.FIELD_PASSING_INSTRUCTIONS);
        PassingInstruction passingInstructions = null;
        if (passingInstructionsObject != null) {
            passingInstructions = PassingInstruction.valueOf(passingInstructionsObject.toString());
        }
        ControlPoint controlPoint = controlPointDeserializer.deserialize((JSONObject) object.get(WaypointJsonSerializer.FIELD_CONTROL_POINT));
        Waypoint waypoint = null;
        if (passingInstructions == null) {
            waypoint = new WaypointImpl(controlPoint);
        } else {
            waypoint = new WaypointImpl(controlPoint, passingInstructions);
        }
        
        return waypoint;
    }

}