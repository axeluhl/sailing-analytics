package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class NationalityJsonSerializer implements JsonSerializer<Nationality> {

    public static final String FIELD_IOC = "IOC";

    @Override
    public JSONObject serialize(Nationality object) {
        if (object == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        result.put(FIELD_IOC, object.getThreeLetterIOCAcronym());
        return result;
    }

}
