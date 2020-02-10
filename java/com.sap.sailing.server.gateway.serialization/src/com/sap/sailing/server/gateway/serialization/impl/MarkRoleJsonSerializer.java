package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkRoleJsonSerializer implements JsonSerializer<MarkRole> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SHORT_NAME = "short_name";

    @Override
    public JSONObject serialize(MarkRole markRole) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markRole.getId().toString());
        result.put(FIELD_NAME, markRole.getName());
        result.put(FIELD_SHORT_NAME, markRole.getShortName());
        return result;
    }
}
