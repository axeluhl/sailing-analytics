package com.sap.sailing.server.gateway.serialization.impl.race;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class WaypointJsonSerializer implements JsonSerializer<Waypoint> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_PASSING_SIDE = "passingSide";
	public static final String FIELD_CONTROL_POINT = "controlPoint";
	
	private final JsonSerializer<ControlPoint> controlPointSerializer;
	
	public WaypointJsonSerializer(JsonSerializer<ControlPoint> controlPointSerializer) {
		this.controlPointSerializer = controlPointSerializer;
	}

	@Override
	public JSONObject serialize(Waypoint object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		if (object.getPassingSide() != null) {
			result.put(FIELD_PASSING_SIDE, object.getPassingSide());
		}
		result.put(FIELD_CONTROL_POINT, 
				controlPointSerializer.serialize(object.getControlPoint()));
		
		return result;
	}

}
