package com.sap.sse.shared.settings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.serializer.AbstractSettingsToJsonSerializer;

/**
 * Serializes a {@link Settings} object to a {@link JSONObject}. All {@link Settings} subclasses are supported as top-level entities.
 * Nesting of {@link Settings} is generally supported.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SettingsToJsonSerializer extends AbstractSettingsToJsonSerializer<JSONObject, JSONArray> {
    @Override
    public String jsonObjectToString(JSONObject jsonObject) {
        return jsonObject.toJSONString();
    }
    
    @Override
    protected JSONObject parseStringToJsonObject(String jsonString) throws Exception {
        return (JSONObject) new JSONParser().parse(jsonString);
    }

    @Override
    protected JSONObject newOBJECT() {
        return new JSONObject();
    }

    @Override
    protected void set(JSONObject jsonObject, String property, Object value) {
        jsonObject.put(property, value);
    }

    @Override
    protected Object get(JSONObject jsonObject, String property) {
        return jsonObject.get(property);
    }
    
    @Override
    protected boolean hasProperty(JSONObject jsonObject, String property) {
        return jsonObject.containsKey(property);
    }
    
    @Override
    protected JSONArray ToJsonArray(Iterable<Object> values) {
        JSONArray jsonArray = new JSONArray();
        for(Object value : values) {
            jsonArray.add(value);
        }
        return jsonArray;
    }

    @Override
    protected Iterable<Object> fromJsonArray(JSONArray jsonArray) {
        return jsonArray;
    }

    @Override
    public boolean isArray(Object jsonValue) {
        return jsonValue instanceof JSONArray;
    }
}
