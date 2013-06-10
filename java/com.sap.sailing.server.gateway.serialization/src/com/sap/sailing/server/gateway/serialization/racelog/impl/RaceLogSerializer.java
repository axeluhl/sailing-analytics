package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogSerializer implements JsonSerializer<RaceLog> {

    public static final String FIELD_EVENTS = "events";
    public static final String FIELD_RACELOG_IDENTIFIER = "racelog_id";
    
    private final JsonSerializer<RaceLogEvent> itemSerializer;
    
    public RaceLogSerializer(JsonSerializer<RaceLogEvent> raceLogEventSerializer) {
        this.itemSerializer = raceLogEventSerializer;
    }
    
    @Override
    public JSONObject serialize(RaceLog object) {
        JSONObject result = new JSONObject();
        object.lockForRead();
        try {
            JSONArray events = new JSONArray();
            for (RaceLogEvent event : object.getRawFixes()) {
                events.add(itemSerializer.serialize(event));
            }
            result.put(FIELD_EVENTS, events);
            result.put(FIELD_RACELOG_IDENTIFIER, object.getId());
        } finally {
            object.unlockAfterRead();
        }
        return result;
    }

}
