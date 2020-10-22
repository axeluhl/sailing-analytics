package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Triple;

public class MarkContextJsonSerializer implements JsonSerializer<Triple<Mark, String, String>> {
    public static final String FIELD_MARK = "mark";
    public static final String FIELD_EVENT_ID = "eventId";
    public static final String FIELD_REGATTA_ID = "regattaId";
    private final MarkJsonSerializer markJsonSerializer;

    public MarkContextJsonSerializer() {
        markJsonSerializer = new MarkJsonSerializer();
    }

    @Override
    public JSONObject serialize(Triple<Mark, String, String> object) {
        JSONObject markContext = new JSONObject();
        markContext.put(FIELD_MARK, markJsonSerializer.serialize(object.getA()));
        markContext.put(FIELD_EVENT_ID, object.getB());
        markContext.put(FIELD_REGATTA_ID, object.getC());
        return markContext;
    }
}
