package com.sap.sailing.selenium.api.event;

import java.util.Map;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class PreferencesApi {

    private static final String PREFERENCES_URL = "/api/v1/preferences/";

    public JSONObject createPreference(ApiContext ctx, String key, Map<String, String> value) {
        JSONObject json = new JSONObject();
        json.putAll(value);
        return ctx.put(PREFERENCES_URL + key, null, json);
    }

    public JSONObject getPreference(ApiContext ctx, String key) {
        return ctx.get(PREFERENCES_URL + key);
    }

    public void deletePreference(ApiContext ctx, String key) {
        ctx.delete(PREFERENCES_URL + key);
    }

}
