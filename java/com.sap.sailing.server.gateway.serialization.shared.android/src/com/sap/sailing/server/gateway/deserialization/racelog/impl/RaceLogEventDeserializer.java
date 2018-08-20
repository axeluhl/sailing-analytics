package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.SmartphoneUUIDIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseBaseDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogAdditionalScoringInformationSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDenoteForTrackingEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDependentStartTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEndOfTrackingEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningListChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFixedMarkPassingEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogGateLineOpeningTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPassChangeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPathfinderEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogProtestStartTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRegisterCompetitorEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRevokeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartOfTrackingEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartProcedureChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartTimeEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartTrackingEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogSuppressedMarkPassingsEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogUseCompetitorsFromRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogWindFixEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.SmartphoneUUIDJsonHandler;

public class RaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {
	
    public static RaceLogEventDeserializer create(SharedDomainFactory domainFactory) {
        JsonDeserializer<DeviceIdentifier> deviceDeserializer = DeviceIdentifierJsonDeserializer.create(
                new SmartphoneUUIDJsonHandler(), SmartphoneUUIDIdentifier.TYPE);
        return create(domainFactory, deviceDeserializer);
    }
	
    public static RaceLogEventDeserializer create(SharedDomainFactory domainFactory,
            JsonDeserializer<DeviceIdentifier> deviceDeserializer) {
    	JsonDeserializer<DynamicCompetitor> competitorDeserializer = CompetitorJsonDeserializer.create(domainFactory);
        return new RaceLogEventDeserializer(
                new RaceLogFlagEventDeserializer(competitorDeserializer),
                new RaceLogStartTimeEventDeserializer(competitorDeserializer), 
                new RaceLogDependentStartTimeEventDeserializer(competitorDeserializer),
                new RaceLogRaceStatusEventDeserializer(competitorDeserializer),
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
                                new PositionJsonDeserializer())),
                new RaceLogDenoteForTrackingEventDeserializer(competitorDeserializer, domainFactory),
                new RaceLogStartTrackingEventDeserializer(competitorDeserializer),
                new RaceLogRevokeEventDeserializer(competitorDeserializer),
                new RaceLogRegisterCompetitorEventDeserializer(competitorDeserializer, BoatJsonDeserializer.create(domainFactory)),
                new RaceLogAdditionalScoringInformationEventDeserializer(competitorDeserializer),
                new RaceLogFixedMarkPassingEventDeserializer(competitorDeserializer),
                new RaceLogSuppressedMarkPassingsEventDeserializer(competitorDeserializer),
                new RaceLogStartOfTrackingEventDeserializer(competitorDeserializer),
                new RaceLogUseCompetitorsFromRaceLogEventDeserializer(competitorDeserializer),
                new RaceLogEndOfTrackingEventDeserializer(competitorDeserializer));
    }

    protected final JsonDeserializer<RaceLogEvent> flagEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> dependentStartTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> passChangeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startProcedureChangedEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> protestStartTimeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> windFixEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> denoteForTrackingEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startTrackingEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> revokeEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> registerCompetitorEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> additionalScoringInformationEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> fixedMarkPassingEventDeserializer;
    protected final JsonDeserializer<RaceLogEvent> suppressedMarkPassingsDeserializer;
    protected final JsonDeserializer<RaceLogEvent> startOfTrackingDeserializer;
    protected final JsonDeserializer<RaceLogEvent> useCompetitorsFromRaceLogDeserializer;
    protected final JsonDeserializer<RaceLogEvent> endOfTrackingDeserializer;

    public RaceLogEventDeserializer(JsonDeserializer<RaceLogEvent> flagEventDeserializer,
            JsonDeserializer<RaceLogEvent> startTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> dependentStartTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> raceStatusEventDeserializer,
            JsonDeserializer<RaceLogEvent> courseDesignChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningListChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> finishPositioningConfirmedEventDeserializer,
            JsonDeserializer<RaceLogEvent> passChangeEventDeserializer,
            JsonDeserializer<RaceLogEvent> pathfinderEventDeserializer,
            JsonDeserializer<RaceLogEvent> gateLineOpeningTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> startProcedureChangedEventDeserializer,
            JsonDeserializer<RaceLogEvent> protestStartTimeEventDeserializer,
            JsonDeserializer<RaceLogEvent> windFixEventDeserializer,
            JsonDeserializer<RaceLogEvent> denoteForTrackingEventDeserializer,
            JsonDeserializer<RaceLogEvent> startTrackingEventDeserializer,
            JsonDeserializer<RaceLogEvent> revokeEventDeserializer,
            JsonDeserializer<RaceLogEvent> registerCompetitorEventDeserializer,
            JsonDeserializer<RaceLogEvent> additionalScoringInformationEventDeserializer,
            JsonDeserializer<RaceLogEvent> fixedMarkPassingEventDeserializer,
            JsonDeserializer<RaceLogEvent> suppressedMarkPassingsEventDeserializer,
            JsonDeserializer<RaceLogEvent> startOfTrackingEventDeserializer,
            JsonDeserializer<RaceLogEvent> useCompetitorsFromRaceLogDeserializer,
            JsonDeserializer<RaceLogEvent> endOfTrackingEventDeserializer) {
        this.flagEventDeserializer = flagEventDeserializer;
        this.startTimeEventDeserializer = startTimeEventDeserializer;
        this.dependentStartTimeEventDeserializer = dependentStartTimeEventDeserializer;
        this.raceStatusEventDeserializer = raceStatusEventDeserializer;
        this.courseDesignChangedEventDeserializer = courseDesignChangedEventDeserializer;
        this.finishPositioningListChangedEventDeserializer = finishPositioningListChangedEventDeserializer;
        this.finishPositioningConfirmedEventDeserializer = finishPositioningConfirmedEventDeserializer;
        this.passChangeEventDeserializer = passChangeEventDeserializer;
        this.pathfinderEventDeserializer = pathfinderEventDeserializer;
        this.gateLineOpeningTimeEventDeserializer = gateLineOpeningTimeEventDeserializer;
        this.startProcedureChangedEventDeserializer = startProcedureChangedEventDeserializer;
        this.protestStartTimeEventDeserializer = protestStartTimeEventDeserializer;
        this.windFixEventDeserializer = windFixEventDeserializer;
        this.denoteForTrackingEventDeserializer = denoteForTrackingEventDeserializer;
        this.startTrackingEventDeserializer = startTrackingEventDeserializer;
        this.revokeEventDeserializer = revokeEventDeserializer;
        this.registerCompetitorEventDeserializer = registerCompetitorEventDeserializer;
        this.additionalScoringInformationEventDeserializer = additionalScoringInformationEventDeserializer;
        this.fixedMarkPassingEventDeserializer = fixedMarkPassingEventDeserializer;
        this.suppressedMarkPassingsDeserializer = suppressedMarkPassingsEventDeserializer;
        this.startOfTrackingDeserializer = startOfTrackingEventDeserializer;
        this.endOfTrackingDeserializer = endOfTrackingEventDeserializer;
        this.useCompetitorsFromRaceLogDeserializer = useCompetitorsFromRaceLogDeserializer;
    }

    protected JsonDeserializer<RaceLogEvent> getDeserializer(JSONObject object) throws JsonDeserializationException {
        String type = object.get(BaseRaceLogEventSerializer.FIELD_CLASS).toString();

        if (type.equals(RaceLogFlagEventSerializer.VALUE_CLASS)) {
            return flagEventDeserializer;
        } else if (type.equals(RaceLogStartTimeEventSerializer.VALUE_CLASS)) {
            return startTimeEventDeserializer;
        } else if (type.equals(RaceLogDependentStartTimeEventSerializer.VALUE_CLASS)) {
            return dependentStartTimeEventDeserializer;
        } else if (type.equals(RaceLogRaceStatusEventSerializer.VALUE_CLASS)) {
            return raceStatusEventDeserializer;
        } else if (type.equals(RaceLogPassChangeEventSerializer.VALUE_CLASS)) {
            return passChangeEventDeserializer;
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
        } else if (type.equals(RaceLogDenoteForTrackingEventSerializer.VALUE_CLASS)) {
            return denoteForTrackingEventDeserializer;
        } else if (type.equals(RaceLogStartTrackingEventSerializer.VALUE_CLASS)) {
            return startTrackingEventDeserializer;
        } else if (type.equals(RaceLogRevokeEventSerializer.VALUE_CLASS)) {
            return revokeEventDeserializer;
        } else if (type.equals(RaceLogRegisterCompetitorEventSerializer.VALUE_CLASS)) {
            return registerCompetitorEventDeserializer;
        } else if (type.equals(RaceLogAdditionalScoringInformationSerializer.VALUE_CLASS)) {
            return additionalScoringInformationEventDeserializer;
        } else if (type.equals(RaceLogFixedMarkPassingEventSerializer.VALUE_CLASS)) {
            return fixedMarkPassingEventDeserializer;
        } else if (type.equals(RaceLogSuppressedMarkPassingsEventSerializer.VALUE_CLASS)){
            return suppressedMarkPassingsDeserializer;
        } else if (type.equalsIgnoreCase(RaceLogStartOfTrackingEventSerializer.VALUE_CLASS)){
            return startOfTrackingDeserializer;
        } else if (type.equals(RaceLogEndOfTrackingEventSerializer.VALUE_CLASS)){
            return endOfTrackingDeserializer;
        } else if (type.equals(RaceLogUseCompetitorsFromRaceLogEventSerializer.VALUE_CLASS)){
            return useCompetitorsFromRaceLogDeserializer;
        }

        throw new JsonDeserializationException(String.format("There is no deserializer defined for event type %s.",
                type));
    }

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        return getDeserializer(object).deserialize(object);
    }
}