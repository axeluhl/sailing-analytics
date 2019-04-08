package com.sap.sse.gwt.settings;

import java.util.ArrayList;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.serializer.AbstractSettingsToJsonSerializer;

/**
 * This is a GWT implementation of an {@link AbstractSettingsToJsonSerializer} that uses the json api library available
 * on the client side.
 *
 */
public class SettingsToJsonSerializerGWT extends AbstractSettingsToJsonSerializer<JSONObject, JSONArray> {
    
    @Override
    public String jsonObjectToString(JSONObject jsonObject) {
        return jsonObject.toString();
    }
    
    @Override
    public JSONObject parseStringToJsonObject(String jsonString) {
        JSONValue jsonValue = JSONParser.parseStrict(jsonString);
        if (jsonValue != null) {
            JSONObject jsonObject = jsonValue.isObject();
            if (jsonObject != null) {
                return jsonObject;
            }
        }
        return null;
    }
    
    @Override
    protected JSONObject newOBJECT() {
        return new JSONObject();
    }

    @Override
    protected void set(JSONObject jsonObject, String property, Object value) {
        jsonObject.put(property, toJsonValue(value));
    }
    
    private JSONValue toJsonValue(Object value) {
        if(value == null) {
            return JSONNull.getInstance();
        } else if (value instanceof JSONArray) {
            return (JSONArray) value;
        } else if (value instanceof JSONObject) {
            return (JSONObject) value;
        } else if (value instanceof String) {
            return new JSONString((String) value);
        } else if (value instanceof Number) {
            return new JSONNumber(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            return JSONBoolean.getInstance((Boolean) value);
        }
        throw new IllegalStateException("Non JSON-compatible value found!");
    }
    
    private Object fromJsonValue(JSONValue jsonValue) {
        if(jsonValue.isNull() != null) {
            return null;
        }
        if(jsonValue.isBoolean() != null) {
            return jsonValue.isBoolean().booleanValue();
        }
        if(jsonValue.isString() != null) {
            return jsonValue.isString().stringValue();
        }
        if(jsonValue.isNumber() != null) {
            return jsonValue.isNumber().doubleValue();
        }
        if(jsonValue.isObject() != null) {
            return jsonValue.isObject();
        }
        if(jsonValue.isArray() != null) {
            return jsonValue.isArray();
        }
        throw new IllegalStateException("Unexpected JSONValue found!");
    }

    @Override
    protected Object get(JSONObject jsonObject, String property) {
        return fromJsonValue(jsonObject.get(property));
    }

    @Override
    protected boolean hasProperty(JSONObject jsonObject, String property) {
        return jsonObject.containsKey(property);
    }

    @Override
    protected JSONArray ToJsonArray(Iterable<Object> values) {
        JSONArray jsonArray = new JSONArray();
        for (Object value : values) {
            JSONValue jsonValue = toJsonValue(value);
            if (jsonValue != null) {
                jsonArray.set(jsonArray.size(), jsonValue);
            }
        }
        return jsonArray;
    }

    @Override
    protected Iterable<Object> fromJsonArray(JSONArray jsonArray) {
        final ArrayList<Object> result = new ArrayList<Object>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(fromJsonValue(jsonArray.get(i)));
        }
        return result;
    }

    @Override
    public boolean isArray(Object jsonValue) {
        return jsonValue instanceof JSONArray;
    }
}
