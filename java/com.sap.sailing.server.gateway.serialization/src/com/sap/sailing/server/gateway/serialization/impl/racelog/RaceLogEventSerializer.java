package com.sap.sailing.server.gateway.serialization.impl.racelog;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {

    public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer) {
        return new RaceLogEventSerializer(new RaceLogEventSerializerChooser(
                new RaceLogFlagEventSerializer(competitorSerializer), 
                new RaceLogStartTimeEventSerializer(competitorSerializer), 
                new RaceLogRaceStatusEventSerializer(competitorSerializer),
                new RaceLogCourseAreaChangedEventSerializer(competitorSerializer)));
    }

    private RaceLogEventSerializerChooser serializerChooser;

    public RaceLogEventSerializer(RaceLogEventSerializerChooser serializerChooser) {
        this.serializerChooser = serializerChooser;
    }

    protected JsonSerializer<RaceLogEvent> getSerializer(RaceLogEvent event) {
        return serializerChooser.getSerializer(event);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        return getSerializer(object).serialize(object);
    }

}
