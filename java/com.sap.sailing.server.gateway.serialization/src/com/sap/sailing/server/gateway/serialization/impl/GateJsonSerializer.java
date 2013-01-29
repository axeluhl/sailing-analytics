package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class GateJsonSerializer extends BaseControlPointJsonSerializer implements JsonSerializer<ControlPoint> {
	public static final String VALUE_CLASS = Gate.class.getSimpleName();
	
	public static final String FIELD_LEFT = "left";
	public static final String FIELD_RIGHT = "right";
	
	private JsonSerializer<ControlPoint> markSerializer;
	
	public GateJsonSerializer(JsonSerializer<ControlPoint> markSerializer) {
		this.markSerializer = markSerializer;
	}

	@Override
	protected String getClassFieldValue() {
		return VALUE_CLASS;
	}
	
	@Override
	public JSONObject serialize(ControlPoint object) {
		Gate gate = (Gate) object;
		JSONObject result =  super.serialize(gate);
		
		result.put(FIELD_LEFT, markSerializer.serialize(gate.getLeft()));
		result.put(FIELD_RIGHT, markSerializer.serialize(gate.getRight()));
		
		return result;
	}

}
