package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

public class ColorJsonSerializer implements JsonSerializer<Color> {
    public static final String FIELD_RED = "r";
    public static final String FIELD_GREEN = "g";
    public static final String FIELD_BLUE = "b";

    @Override
    public JSONObject serialize(Color color) {
        JSONObject result = new JSONObject();

        Util.Triple<Integer, Integer, Integer> rgb = color.getAsRGB();
        result.put(FIELD_RED, rgb.getA());
        result.put(FIELD_GREEN, rgb.getB());
        result.put(FIELD_BLUE, rgb.getC());

        return result;
    }

}
