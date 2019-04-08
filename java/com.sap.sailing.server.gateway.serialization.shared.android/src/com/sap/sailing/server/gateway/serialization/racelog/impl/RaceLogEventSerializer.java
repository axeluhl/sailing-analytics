package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.SmartphoneUUIDIdentifier;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PositionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WindJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.SmartphoneUUIDJsonHandler;

public class RaceLogEventSerializer implements JsonSerializer<RaceLogEvent>, RaceLogEventVisitor {
    public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer) {
        return create(competitorSerializer, DeviceIdentifierJsonSerializer.create(
                new SmartphoneUUIDJsonHandler(), SmartphoneUUIDIdentifier.TYPE));
    }
    
    public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer,
    		JsonSerializer<DeviceIdentifier> deviceSerializer) {
        return new RaceLogEventSerializer(
                new RaceLogFlagEventSerializer(competitorSerializer), 
                new RaceLogStartTimeEventSerializer(competitorSerializer), 
                new RaceLogRaceStatusEventSerializer(competitorSerializer),
                new RaceLogPassChangeEventSerializer(competitorSerializer),
                new RaceLogCourseDesignChangedEventSerializer(competitorSerializer,
                        new CourseBaseJsonSerializer(
                                new WaypointJsonSerializer(
                                        new ControlPointJsonSerializer(
                                                new MarkJsonSerializer(),
                                                new GateJsonSerializer(new MarkJsonSerializer()))))),
                new RaceLogFinishPositioningListChangedEventSerializer(competitorSerializer),
                new RaceLogFinishPositioningConfirmedEventSerializer(competitorSerializer),
                new RaceLogPathfinderEventSerializer(competitorSerializer),
                new RaceLogGateLineOpeningTimeEventSerializer(competitorSerializer),
                new RaceLogStartProcedureChangedEventSerializer(competitorSerializer),
                new RaceLogProtestStartTimeEventSerializer(competitorSerializer),
                new RaceLogWindFixEventSerializer(competitorSerializer, 
                        new WindJsonSerializer(
                                new PositionJsonSerializer())),
                new RaceLogDenoteForTrackingEventSerializer(competitorSerializer),
                new RaceLogStartTrackingEventSerializer(competitorSerializer),
                new RaceLogRevokeEventSerializer(competitorSerializer),
                new RaceLogRegisterCompetitorEventSerializer(competitorSerializer, BoatJsonSerializer.create()),
                new RaceLogAdditionalScoringInformationSerializer(competitorSerializer),
                new RaceLogFixedMarkPassingEventSerializer(competitorSerializer),
                new RaceLogSuppressedMarkPassingsEventSerializer(competitorSerializer),
                new RaceLogDependentStartTimeEventSerializer(competitorSerializer),
                new RaceLogStartOfTrackingEventSerializer(competitorSerializer),
                new RaceLogUseCompetitorsFromRaceLogEventSerializer(competitorSerializer),
                new RaceLogEndOfTrackingEventSerializer(competitorSerializer),
                new RaceLogTagEventSerializer(competitorSerializer));
    }

    private final JsonSerializer<RaceLogEvent> flagEventSerializer;
    private final JsonSerializer<RaceLogEvent> startTimeSerializer;
    private final JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private final JsonSerializer<RaceLogEvent> passChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer;
    private final JsonSerializer<RaceLogEvent> pathfinderEventSerializer;
    private final JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer;
    private final JsonSerializer<RaceLogEvent> startProcedureChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> protestStartTimeEventSerializer;
    private final JsonSerializer<RaceLogEvent> windFixEventSerializer;
    private final JsonSerializer<RaceLogEvent> denoteForTrackingSerializer;
    private final JsonSerializer<RaceLogEvent> startTrackingSerializer;
    private final JsonSerializer<RaceLogEvent> revokeSerializer;
    private final JsonSerializer<RaceLogEvent> registerCompetitorSerializer;
    private final JsonSerializer<RaceLogEvent> additionalScoringInformationSerializer;
    private final JsonSerializer<RaceLogEvent> fixedMarkPassingEventSerializer;
    private final JsonSerializer<RaceLogEvent> suppressedMarkPassingsEventSerializer;
    private final JsonSerializer<RaceLogEvent> dependentStartTimeEventSerializer;
    private final JsonSerializer<RaceLogEvent> startOfTrackingEventSerializer;
    private final JsonSerializer<RaceLogEvent> useCompetitorsFromRaceLogEventSerializer;
    private final JsonSerializer<RaceLogEvent> endOfTrackingEventSerializer;
    private final JsonSerializer<RaceLogEvent> tagSerializer;

    private JsonSerializer<RaceLogEvent> chosenSerializer;


    public RaceLogEventSerializer(
            JsonSerializer<RaceLogEvent> flagEventSerializer,
            JsonSerializer<RaceLogEvent> startTimeSerializer,
            JsonSerializer<RaceLogEvent> raceStatusSerializer,
            JsonSerializer<RaceLogEvent> passChangedEventSerializer,
            JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer, 
            JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer,
            JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer,
            JsonSerializer<RaceLogEvent> pathfinderEventSerializer,
            JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer,
            JsonSerializer<RaceLogEvent> startProcedureChangedEventSerializer,
            JsonSerializer<RaceLogEvent> protestStartTimeEventSerializer,
            JsonSerializer<RaceLogEvent> windFixEventSerializer,
            JsonSerializer<RaceLogEvent> denoteForTrackingSerializer,
            JsonSerializer<RaceLogEvent> createRaceSerializer,
            JsonSerializer<RaceLogEvent> revokeSerializer,
            JsonSerializer<RaceLogEvent> registerCompetitorSerializer,
            JsonSerializer<RaceLogEvent> additionalScoringInformationSerializer,
            JsonSerializer<RaceLogEvent> fixedMarkPassingEventSerializer,
            JsonSerializer<RaceLogEvent> suppressedMarkPassingsSerializer,
            JsonSerializer<RaceLogEvent> dependentStartTimeEventSerializer,
            JsonSerializer<RaceLogEvent> startOfTrackingEventSerializer,
            JsonSerializer<RaceLogEvent> useCompetitorsFromRaceLogEventSerializer,
            JsonSerializer<RaceLogEvent> endOfTrackingEventSerializer,
            JsonSerializer<RaceLogEvent> tagSerializer) {

        this.flagEventSerializer = flagEventSerializer;
        this.startTimeSerializer = startTimeSerializer;
        this.raceStatusSerializer = raceStatusSerializer;
        this.passChangedEventSerializer = passChangedEventSerializer;
        this.courseDesignChangedEventSerializer = courseDesignChangedEventSerializer;
        this.finishPositioningListChangedEventSerializer = finishPositioningListChangedEventSerializer;
        this.finishPositioningConfirmedEventSerializer = finishPositioningConfirmedEventSerializer;
        this.pathfinderEventSerializer = pathfinderEventSerializer;
        this.gateLineOpeningTimeEventSerializer = gateLineOpeningTimeEventSerializer;
        this.startProcedureChangedEventSerializer = startProcedureChangedEventSerializer;
        this.protestStartTimeEventSerializer = protestStartTimeEventSerializer;
        this.windFixEventSerializer = windFixEventSerializer;
        this.denoteForTrackingSerializer = denoteForTrackingSerializer;
        this.startTrackingSerializer = createRaceSerializer;
        this.revokeSerializer = revokeSerializer;
        this.registerCompetitorSerializer = registerCompetitorSerializer;
        this.additionalScoringInformationSerializer = additionalScoringInformationSerializer;
        this.fixedMarkPassingEventSerializer = fixedMarkPassingEventSerializer;
        this.suppressedMarkPassingsEventSerializer = suppressedMarkPassingsSerializer;
        this.dependentStartTimeEventSerializer = dependentStartTimeEventSerializer;
        this.startOfTrackingEventSerializer = startOfTrackingEventSerializer;
        this.endOfTrackingEventSerializer = endOfTrackingEventSerializer;
        this.useCompetitorsFromRaceLogEventSerializer = useCompetitorsFromRaceLogEventSerializer;
        this.tagSerializer = tagSerializer;
        
        this.chosenSerializer = null;
    }

    protected JsonSerializer<RaceLogEvent> getSerializer(RaceLogEvent event) {
        chosenSerializer = null;
        event.accept(this);
        if (chosenSerializer == null) {
            throw new UnsupportedOperationException(
                    String.format("There is no serializer for event type %s", 
                            event.getClass().getName()));
        }
        return chosenSerializer;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        return getSerializer(object).serialize(object);
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
        chosenSerializer = flagEventSerializer;
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        chosenSerializer = passChangedEventSerializer;
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        chosenSerializer = raceStatusSerializer;
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        chosenSerializer = startTimeSerializer;
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        chosenSerializer = courseDesignChangedEventSerializer;
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        chosenSerializer = finishPositioningListChangedEventSerializer;
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        chosenSerializer = finishPositioningConfirmedEventSerializer;        
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        chosenSerializer = pathfinderEventSerializer;
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        chosenSerializer = gateLineOpeningTimeEventSerializer;
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        chosenSerializer = startProcedureChangedEventSerializer;
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        chosenSerializer = protestStartTimeEventSerializer;
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        chosenSerializer = windFixEventSerializer;
    }

    @Override
    public void visit(RaceLogDenoteForTrackingEvent event) {
        chosenSerializer = denoteForTrackingSerializer;
    }
    @Override
    public void visit(RaceLogStartTrackingEvent event) {
        chosenSerializer = startTrackingSerializer;
    }
    @Override
    public void visit(RaceLogRevokeEvent event) {
        chosenSerializer = revokeSerializer;
    }

    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {
        chosenSerializer = registerCompetitorSerializer;
    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation) {
        chosenSerializer = additionalScoringInformationSerializer;
    }
    
    @Override
    public void visit(RaceLogFixedMarkPassingEvent event) {
        chosenSerializer = fixedMarkPassingEventSerializer;
    }

    @Override
    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
        chosenSerializer = suppressedMarkPassingsEventSerializer;        
    }
    
    @Override
    public void visit(RaceLogDependentStartTimeEvent event) {
        chosenSerializer = dependentStartTimeEventSerializer;        
    }

    @Override
    public void visit(RaceLogStartOfTrackingEvent event) {
        chosenSerializer = startOfTrackingEventSerializer;    
    }

    @Override
    public void visit(RaceLogEndOfTrackingEvent event) {
        chosenSerializer = endOfTrackingEventSerializer;        
    }

    @Override
    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
        chosenSerializer = useCompetitorsFromRaceLogEventSerializer;
    }
    
    @Override
    public void visit(RaceLogTagEvent event) {
        chosenSerializer = tagSerializer;
    }
}
