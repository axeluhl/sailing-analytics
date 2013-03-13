package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.RaceLogEventSerializerChooser;

public class RaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {

    public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer) {
        return new RaceLogEventSerializer(new RaceLogEventSerializerChooserImpl(
                new RaceLogFlagEventSerializer(competitorSerializer), 
                new RaceLogStartTimeEventSerializer(competitorSerializer), 
                new RaceLogRaceStatusEventSerializer(competitorSerializer),
                new RaceLogCourseAreaChangedEventSerializer(competitorSerializer),
                new RaceLogPassChangeEventSerializer(competitorSerializer),
                new RaceLogCourseDesignChangedEventSerializer(competitorSerializer,
                        new CourseDataJsonSerializer(
                                new WaypointJsonSerializer(
                                        new ControlPointJsonSerializer(
                                                new MarkJsonSerializer(),
                                                new GateJsonSerializer(new MarkJsonSerializer())))))));
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
