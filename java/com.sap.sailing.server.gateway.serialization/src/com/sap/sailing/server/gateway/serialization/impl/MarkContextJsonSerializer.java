package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.dto.MarkContext;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;

public class MarkContextJsonSerializer implements JsonSerializer<MarkContext> {
    public static final String FIELD_MARK_KEY = "mark";
    public static final String FIELD_EVENT_KEY = "event";
    public static final String FIELD_EVENT_NAME = "name";
    public static final String FIELD_EVENT_ID = "eventId";
    public static final String FIELD_REGATTA_KEY = "regatta";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    private final MarkJsonSerializer markJsonSerializer;

    public MarkContextJsonSerializer() {
        markJsonSerializer = new MarkJsonSerializer();
    }

    @Override
    public JSONObject serialize(MarkContext markContext) {
        JSONObject jsonMarkContext = new JSONObject();
        jsonMarkContext.put(FIELD_MARK_KEY, markJsonSerializer.serialize(markContext.getMark()));
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(FIELD_EVENT_ID, markContext.getEvent().getId());
        jsonEvent.put(FIELD_EVENT_NAME, markContext.getEvent().getName());
        jsonMarkContext.put(FIELD_EVENT_KEY, jsonEvent);
        JSONObject jsonRegatta = new JSONObject();
        jsonRegatta.put(FIELD_REGATTA_NAME, markContext.getRegatta().getName());
        jsonMarkContext.put(FIELD_REGATTA_KEY, jsonRegatta);
        return jsonMarkContext;
    }
}
