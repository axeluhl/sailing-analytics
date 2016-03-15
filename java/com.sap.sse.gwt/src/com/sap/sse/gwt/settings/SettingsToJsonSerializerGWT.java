package com.sap.sse.gwt.settings;

import java.util.ArrayList;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
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
    protected JSONObject newOBJECT() {
        return new JSONObject();
    }

    @Override
    protected void set(JSONObject jsonObject, String property, Object value) {

        if (value instanceof String) {
            jsonObject.put(property, new JSONString((String) value));
        } else if (value instanceof Number) {
            jsonObject.put(property, new JSONNumber(((Number) value).doubleValue()));
        } else if (value instanceof Boolean) {
            jsonObject.put(property, JSONBoolean.getInstance((Boolean) value));
        }

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
        for (Object value : values) {
            JSONValue jsonValue = null;
            if (value instanceof String) {
                jsonValue = new JSONString((String) value);
            } else if (value instanceof Number) {
                jsonValue = new JSONNumber(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                jsonValue = JSONBoolean.getInstance((Boolean) value);
            }
            if (jsonValue != null) {
                jsonArray.set(jsonArray.size(), jsonValue);
            }
        }
        return jsonArray;
    }

    @Override
    protected Iterable<Object> fromJsonArray(JSONArray jsonArray) {
        final ArrayList<Object> result = new ArrayList<Object>(jsonArray.size());
        toLoop: for (int i = 0; i < jsonArray.size(); i++) {
            JSONValue jsonValue = jsonArray.get(i);
            JSONString jS;
            if ((jS = jsonValue.isString()) != null) {
                result.add(jS.stringValue());
                continue toLoop;
            }
            JSONBoolean jB;
            if ((jB = jsonValue.isBoolean()) != null) {
                result.add(jB.booleanValue());
                continue toLoop;
            }
            JSONNumber jN;
            if ((jN = jsonValue.isNumber()) != null) {
                result.add(jN.doubleValue());
                continue toLoop;
            }
        }
        return result;
    }
}
