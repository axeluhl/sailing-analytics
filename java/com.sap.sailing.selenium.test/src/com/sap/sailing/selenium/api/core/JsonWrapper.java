package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;

public class JsonWrapper {

    private JSONObject json;

    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) json.get(key);
    }
}
