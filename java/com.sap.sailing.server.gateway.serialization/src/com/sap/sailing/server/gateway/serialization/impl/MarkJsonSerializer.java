package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkJsonSerializer extends BaseControlPointJsonSerializer implements JsonSerializer<ControlPoint> {
	public static final String VALUE_CLASS = Mark.class.getSimpleName();
	
	public static final String FIELD_ID = "id";
	public static final String FIELD_COLOR = "color";
	public static final String FIELD_PATTERN = "pattern";
	public static final String FIELD_SHAPE = "shape";
	public static final String FIELD_TYPE = "type";

	@Override
	protected String getClassFieldValue() {
		return VALUE_CLASS;
	}
	
	@Override
	public JSONObject serialize(ControlPoint object) {
		Mark mark = (Mark) object;
		JSONObject result = super.serialize(mark);
		
		result.put(FIELD_ID, mark.getId().toString());
		result.put(FIELD_COLOR, mark.getColor());
		result.put(FIELD_PATTERN, mark.getPattern());
		result.put(FIELD_SHAPE, mark.getShape());
		result.put(FIELD_TYPE, mark.getType().toString());
		
		return result;
	}

}
