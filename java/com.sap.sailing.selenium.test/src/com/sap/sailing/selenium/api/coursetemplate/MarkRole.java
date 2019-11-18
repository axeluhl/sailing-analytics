package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class MarkRole extends JsonWrapper {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    public MarkRole(JSONObject json) {
        super(json);
    }

    public UUID getId() {
        final String uuid = get(FIELD_ID);
        return uuid != null ? UUID.fromString(uuid) : null;
    }

    public String getName() {
        return get(FIELD_NAME);
    }
}
