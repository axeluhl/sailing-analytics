package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatClassJsonSerializer implements JsonSerializer<BoatClass> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPICALLY_STARTS_UPWIND = "typicallyStartsUpwind";

    @Override
    public JSONObject serialize(BoatClass boatClass) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, boatClass.getName());
        result.put(FIELD_TYPICALLY_STARTS_UPWIND, boatClass.typicallyStartsUpwind());
        return result;
    }
}
