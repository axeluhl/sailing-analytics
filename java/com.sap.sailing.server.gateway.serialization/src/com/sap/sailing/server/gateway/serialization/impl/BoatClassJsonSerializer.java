package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatClassJsonSerializer implements JsonSerializer<BoatClass> {
    public static final String FIELD_NAME = "name";

    @Override
    public JSONObject serialize(BoatClass object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        return result;
    }

}
