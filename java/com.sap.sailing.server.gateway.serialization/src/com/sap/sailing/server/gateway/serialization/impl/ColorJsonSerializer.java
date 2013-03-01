package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class ColorJsonSerializer implements JsonSerializer<Color> {
    public static final String FIELD_RED = "r";
    public static final String FIELD_GREEN = "g";
    public static final String FIELD_BLUE = "b";

    @Override
    public JSONObject serialize(Color object) {
        JSONObject result = new JSONObject();

        Triple<Integer, Integer, Integer> rgb = object.getAsRGB();
        result.put(FIELD_RED, rgb.getA());
        result.put(FIELD_GREEN, rgb.getB());
        result.put(FIELD_BLUE, rgb.getC());

        return result;
    }

}
