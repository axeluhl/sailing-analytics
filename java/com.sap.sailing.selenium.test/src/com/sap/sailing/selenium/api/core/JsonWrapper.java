package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;

public class JsonWrapper {

    private final JSONObject json;

    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    public boolean isEmpty() {
        return json.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) json.get(key);
    }

    @Override
    public String toString() {
        return json.toJSONString();
    }

}
