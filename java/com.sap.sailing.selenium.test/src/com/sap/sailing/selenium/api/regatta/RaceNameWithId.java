package com.sap.sailing.selenium.api.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RaceNameWithId extends JsonWrapper {

    public RaceNameWithId(JSONObject json) {
        super(json);
    }

    public String getName() {
        return get("name");
    }

    public String getId() {
        return get("id");
    }
}