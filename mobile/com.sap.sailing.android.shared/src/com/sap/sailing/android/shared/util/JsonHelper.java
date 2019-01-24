package com.sap.sailing.android.shared.util;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Helps converting JSON objects coming from the {@code org.json.JSONObject} world, as used in the Android world, to the
 * {@code org.json.simple.JSONObject} world. The approach works by naively {@link String}-serializing the
 * {@link org.json.JSONObject} and then parsing this {@link String} again using a {@link JSONParser} from the
 * {@code org.json.simple} world.
 */
public class JsonHelper {
    public static org.json.simple.JSONObject convertToSimple(JSONObject jsonObject) throws ParseException {
        String jsonString = jsonObject.toString();
        JSONParser jsonParser = new JSONParser();
        return (org.json.simple.JSONObject) jsonParser.parse(jsonString);
    }
}
