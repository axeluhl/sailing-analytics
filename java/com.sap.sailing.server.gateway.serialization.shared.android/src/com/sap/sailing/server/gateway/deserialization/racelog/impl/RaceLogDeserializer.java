package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogSerializer;

public class RaceLogDeserializer implements JsonDeserializer<RaceLog> {

    private final JsonDeserializer<RaceLogEvent> elementDeserializer;
    
    public RaceLogDeserializer(JsonDeserializer<RaceLogEvent> raceLogEventDeserializer) {
        this.elementDeserializer = raceLogEventDeserializer;
    }
    
    public RaceLog deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable id = (String)object.get(RaceLogSerializer.FIELD_RACELOG_IDENTIFIER);
        RaceLog result = new RaceLogImpl(id);
        if (object.get(RaceLogSerializer.FIELD_EVENTS) == null) {
            return result;
        }
        JSONArray events = Helpers.getNestedArraySafe(object, RaceLogSerializer.FIELD_EVENTS);
        for (Object eventObject : events) {
            JSONObject eventJson = Helpers.toJSONObjectSafe(eventObject);
            RaceLogEvent event = elementDeserializer.deserialize(eventJson);
            result.add(event);
        }
        return result;
    }

}
