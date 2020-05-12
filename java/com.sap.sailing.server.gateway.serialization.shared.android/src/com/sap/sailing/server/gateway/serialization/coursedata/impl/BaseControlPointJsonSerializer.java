package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class BaseControlPointJsonSerializer implements JsonSerializer<ControlPoint> {
    public static final String FIELD_CLASS = "@class";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SHORT_NAME = "shortName";

    protected abstract String getClassFieldValue();

    @Override
    public JSONObject serialize(ControlPoint object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_CLASS, getClassFieldValue());
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_SHORT_NAME, object.getShortName());
        return result;
    }

}
