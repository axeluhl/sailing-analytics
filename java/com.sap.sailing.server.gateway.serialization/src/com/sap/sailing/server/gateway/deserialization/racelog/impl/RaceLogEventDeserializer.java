package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseDataDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningListChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogGateLineOpeningTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPassChangeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPathfinderEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartTimeEventSerializer;

public class RaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {
    
    public static RaceLogEventDeserializer create(SharedDomainFactory domainFactory) {
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorDeserializer(domainFactory);
        return new RaceLogEventDeserializer(
                new RaceLogFlagEventDeserializer(competitorDeserializer),
                new RaceLogStartTimeEventDeserializer(competitorDeserializer), 
                new RaceLogRaceStatusEventDeserializer(competitorDeserializer),
                new RaceLogCourseAreaChangedEventDeserializer(competitorDeserializer),
                new RaceLogCourseDesignChangedEventDeserializer(competitorDeserializer,
                        new CourseDataDeserializer(
                                new WaypointDeserializer(
                                        new ControlPointDeserializer(
                                                new MarkDeserializer(domainFactory), new GateDeserializer(domainFactory, new MarkDeserializer(domainFactory)))))),
                new RaceLogFinishPositioningListChangedEventDeserializer(competitorDeserializer),
                new RaceLogFinishPositioningConfirmedEventDeserializer(competitorDeserializer),
                new RaceLogPassChangeEventDeserializer(competitorDeserializer),
                new RaceLogPathFinderEventDeserializer(competitorDeserializer),
                new RaceLogGateLineOpeningTimeEventDeserializer(competitorDeserializer));
    }

    protected JsonDeserializer<RaceLogEvent> flagEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> startTimeEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> passChangeEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer;
    protected JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer;

    public RaceLogEventDeserializer(JsonDeserializer<RaceLogEvent> flagEventDeserializer,
            JsonDeserializer<RaceLogEvent> startTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer,
            JsonDeserializer<RaceLogEvent> passChangeEventDeserializer,
            JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer,
            JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer) {
        this.flagEventDeserializer = flagEventDeserializer;
        this.startTimeEventDeserializer = startTimeEventDeserializer;
        this.raceStatusEventDeserializer = raceStatusEventDeserializer;
        this.courseAreaChangedEventDeserializer = courseAreaChangedEventDeserializer;
        this.courseDesignChangedEventDeserializer = courseDesignChangedEventDeserializer;
        this.finishPositioningListChangedEventDeserializer = finishPositioningListChangedEventDeserializer;
        this.finishPositioningConfirmedEventDeserializer = finishPositioningConfirmedEventDeserializer;
        this.passChangeEventDeserializer = passChangeEventDeserializer;
        this.pathfinderEventDeserializer = pathfinderEventDeserializer;
        this.gateLineOpeningTimeEventDeserializer = gateLineOpeningTimeEventDeserializer;
    }

    protected JsonDeserializer<RaceLogEvent> getDeserializer(JSONObject object) throws JsonDeserializationException {
        String type = object.get(BaseRaceLogEventSerializer.FIELD_CLASS).toString();

        if (type.equals(RaceLogFlagEventSerializer.VALUE_CLASS)) {
            return flagEventDeserializer;
        } else if (type.equals(RaceLogStartTimeEventSerializer.VALUE_CLASS)) {
            return startTimeEventDeserializer;
        } else if (type.equals(RaceLogRaceStatusEventSerializer.VALUE_CLASS)) {
            return raceStatusEventDeserializer;
        } else if (type.equals(RaceLogPassChangeEventSerializer.VALUE_CLASS)) {
            return passChangeEventDeserializer;
        } else if (type.equals(RaceLogCourseAreaChangedEventSerializer.VALUE_CLASS)) {
            return courseAreaChangedEventDeserializer;
        } else if (type.equals(RaceLogCourseDesignChangedEventSerializer.VALUE_CLASS)) {
            return courseDesignChangedEventDeserializer;
        } else if (type.equals(RaceLogFinishPositioningListChangedEventSerializer.VALUE_CLASS)) {
            return finishPositioningListChangedEventDeserializer;
        } else if (type.equals(RaceLogFinishPositioningConfirmedEventSerializer.VALUE_CLASS)) {
            return finishPositioningConfirmedEventDeserializer;
        } else if (type.equals(RaceLogPathfinderEventSerializer.VALUE_CLASS)) {
            return pathfinderEventDeserializer;
        } else if (type.equals(RaceLogGateLineOpeningTimeEventSerializer.VALUE_CLASS)) {
            return gateLineOpeningTimeEventDeserializer;
        }

        throw new JsonDeserializationException(String.format("There is no deserializer defined for event type %s.",
                type));
    }

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        return getDeserializer(object).deserialize(object);
    }
}