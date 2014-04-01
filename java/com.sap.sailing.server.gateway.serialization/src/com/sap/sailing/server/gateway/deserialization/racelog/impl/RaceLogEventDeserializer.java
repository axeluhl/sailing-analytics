package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseBaseDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningListChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogGateLineOpeningTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPassChangeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPathfinderEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogProtestStartTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartProcedureChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogWindFixEventSerializer;

public class RaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {
    
    public static RaceLogEventDeserializer create(SharedDomainFactory domainFactory) {
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorJsonDeserializer(domainFactory, null, /* boatDeserializer */ null);
        return new RaceLogEventDeserializer(
                new RaceLogFlagEventDeserializer(competitorDeserializer),
                new RaceLogStartTimeEventDeserializer(competitorDeserializer), 
                new RaceLogRaceStatusEventDeserializer(competitorDeserializer),
                new RaceLogCourseAreaChangedEventDeserializer(competitorDeserializer),
                new RaceLogCourseDesignChangedEventDeserializer(competitorDeserializer,
                        new CourseBaseDeserializer(
                                new WaypointDeserializer(
                                        new ControlPointDeserializer(
                                                new MarkDeserializer(domainFactory), new GateDeserializer(domainFactory, new MarkDeserializer(domainFactory)))))),
                new RaceLogFinishPositioningListChangedEventDeserializer(competitorDeserializer),
                new RaceLogFinishPositioningConfirmedEventDeserializer(competitorDeserializer),
                new RaceLogPassChangeEventDeserializer(competitorDeserializer),
                new RaceLogPathFinderEventDeserializer(competitorDeserializer),
                new RaceLogGateLineOpeningTimeEventDeserializer(competitorDeserializer),
                new RaceLogStartProcedureChangedEventDeserializer(competitorDeserializer),
                new RaceLogProtestStartTimeEventDeserializer(competitorDeserializer),
                new RaceLogWindFixEventDeserializer(competitorDeserializer,
                        new WindJsonDeserializer(
                                new PositionJsonDeserializer())));
    }

    protected final JsonDeserializer<RaceLogEvent> flagEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> passChangeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startProcedureChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> protestStartTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> windFixEventDeserializer;

    public RaceLogEventDeserializer(JsonDeserializer<RaceLogEvent> flagEventDeserializer,
            JsonDeserializer<RaceLogEvent> startTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseAreaChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer,
            JsonDeserializer<RaceLogEvent> passChangeEventDeserializer,
            JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer,
            JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> startProcedureChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> protestStartTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> windFixEventDeserializer) {
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
        this.startProcedureChangedEventDeserializer = startProcedureChangedEventDeserializer;
        this.protestStartTimeEventDeserializer = protestStartTimeEventDeserializer;
        this.windFixEventDeserializer = windFixEventDeserializer;
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
        } else if (type.equals(RaceLogStartProcedureChangedEventSerializer.VALUE_CLASS)) {
            return startProcedureChangedEventDeserializer;
        } else if (type.equals(RaceLogProtestStartTimeEventSerializer.VALUE_CLASS)) {
            return protestStartTimeEventDeserializer;
        } else if (type.equals(RaceLogWindFixEventSerializer.VALUE_CLASS)) {
            return windFixEventDeserializer;
        }

        throw new JsonDeserializationException(String.format("There is no deserializer defined for event type %s.",
                type));
    }

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        return getDeserializer(object).deserialize(object);
    }
}