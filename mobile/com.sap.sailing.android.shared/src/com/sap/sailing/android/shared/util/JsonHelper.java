package com.sap.sailing.android.shared.util;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonHelper {
    public static org.json.simple.JSONObject convertToSimple(JSONObject jsonObject) throws ParseException {
        String jsonString = jsonObject.toString();
        JSONParser jsonParser = new JSONParser();
        return (org.json.simple.JSONObject) jsonParser.parse(jsonString);
    }
}
