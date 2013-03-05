package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogRaceStatusEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogStartTimeEventSerializer;

public class RaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {

    protected JsonDeserializer<RaceLogEvent> flagEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> startTimeEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer;

    public RaceLogEventDeserializer(JsonDeserializer<RaceLogEvent> flagEventDeserializer,
            JsonDeserializer<RaceLogEvent> startTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer) {
        this.flagEventDeserializer = flagEventDeserializer;
        this.startTimeEventDeserializer = startTimeEventDeserializer;
        this.raceStatusEventDeserializer = raceStatusEventDeserializer;
        this.courseAreaChangedEventDeserializer = courseAreaChangedEventDeserializer;
    }

    protected JsonDeserializer<RaceLogEvent> getDeserializer(JSONObject object) throws JsonDeserializationException {
        Object type = object.get(BaseRaceLogEventSerializer.FIELD_CLASS);

        if (type.equals(RaceLogFlagEventSerializer.VALUE_CLASS)) {
            return flagEventDeserializer;
        } else if (type.equals(RaceLogStartTimeEventSerializer.VALUE_CLASS)) {
            return startTimeEventDeserializer;
        } else if (type.equals(RaceLogRaceStatusEventSerializer.VALUE_CLASS)) {
            return raceStatusEventDeserializer;
        } else if (type.equals(RaceLogCourseAreaChangedEventSerializer.VALUE_CLASS)) {
            return courseAreaChangedEventDeserializer;
        }

        throw new JsonDeserializationException(String.format("There is no deserializer defined for event type %s.",
                type));
    }

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        return getDeserializer(object).deserialize(object);
    }
}