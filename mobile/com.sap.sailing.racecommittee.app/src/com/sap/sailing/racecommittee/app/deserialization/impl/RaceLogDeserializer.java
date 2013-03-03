package com.sap.sailing.racecommittee.app.deserialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.PassAwareRaceLogImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogSerializer;

public class RaceLogDeserializer implements JsonDeserializer<RaceLog> {

    private final JsonDeserializer<RaceLogEvent> elementDeserializer;
    
    public RaceLogDeserializer(JsonDeserializer<RaceLogEvent> raceLogEventDeserializer) {
        this.elementDeserializer = raceLogEventDeserializer;
    }
    
    public RaceLog deserialize(JSONObject object) throws JsonDeserializationException {
        RaceLog result = new PassAwareRaceLogImpl();
        
        JSONArray events = Helpers.getNestedArraySafe(object, RaceLogSerializer.FIELD_EVENTS);
        for (Object eventObject : events) {
            JSONObject eventJson = Helpers.toJSONObjectSafe(eventObject);
            RaceLogEvent event = elementDeserializer.deserialize(eventJson);
            result.add(event);
        }
        
        return result;
    }

}
