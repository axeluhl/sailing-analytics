package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sse.shared.json.JsonSerializer;

public class RemoteSailingServerReferenceJsonSerializer implements JsonSerializer<RemoteSailingServerReference> {
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String INCLUDE = "include";
    public static final String EVENT_IDS = "eventIds";

    @Override
    public JSONObject serialize(RemoteSailingServerReference object) {
        final JSONObject result = new JSONObject();
        result.put(NAME, object.getName());
        result.put(URL, object.getURL());
        result.put(INCLUDE, object.isInclude());
        final JSONArray eventIds = new JSONArray();
        object.getSelectedEventIds().forEach(id->eventIds.add(id.toString()));
        result.put(EVENT_IDS, eventIds);
        return result;
    }
}
