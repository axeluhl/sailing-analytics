package com.sap.sailing.selenium.api.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class Course extends JsonWrapper {

    public Course(JSONObject json) {
        super(json);
    }

    public String getName() {
        return get("name");
    }
}
