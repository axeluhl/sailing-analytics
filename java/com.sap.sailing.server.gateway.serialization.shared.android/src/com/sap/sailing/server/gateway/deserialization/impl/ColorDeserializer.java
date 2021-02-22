package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class ColorDeserializer implements JsonDeserializer<Color> {

    public Color deserialize(JSONObject object) throws JsonDeserializationException {
        Number red = (Number) object.get("r");
        Number green = (Number) object.get("g");
        Number blue = (Number) object.get("b");
        return new RGBColor(red.intValue(), green.intValue(), blue.intValue());
    }

}