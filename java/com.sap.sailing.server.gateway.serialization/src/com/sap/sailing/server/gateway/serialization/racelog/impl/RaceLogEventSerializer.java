package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.tracking.SingleTypeBasedServiceFinderImpl;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PositionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WindJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.SmartphoneImeiJsonHandlerImpl;

public class RaceLogEventSerializer implements JsonSerializer<RaceLogEvent>, RaceLogEventVisitor {
	public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer) {
		return create(competitorSerializer,
				new SingleTypeBasedServiceFinderImpl<DeviceIdentifierJsonHandler>(
						new SmartphoneImeiJsonHandlerImpl(), SmartphoneImeiIdentifier.TYPE));
	}
    public static JsonSerializer<RaceLogEvent> create(JsonSerializer<Competitor> competitorSerializer,
    		TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder) {
        return new RaceLogEventSerializer(
                new RaceLogFlagEventSerializer(competitorSerializer), 
                new RaceLogStartTimeEventSerializer(competitorSerializer), 
                new RaceLogRaceStatusEventSerializer(competitorSerializer),
                new RaceLogCourseAreaChangedEventSerializer(competitorSerializer),
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
                new RaceLogDeviceCompetitorMappingEventSerializer(competitorSerializer, deviceServiceFinder),
                new RaceLogDeviceMarkMappingEventSerializer(competitorSerializer, new MarkJsonSerializer(), deviceServiceFinder),
                new RaceLogDenoteForTrackingEventSerializer(competitorSerializer),
                new RaceLogStartTrackingEventSerializer(competitorSerializer),
                new RaceLogRevokeEventSerializer(competitorSerializer),
                new RaceLogRegisterCompetitorEventSerializer(competitorSerializer),
                new RaceLogDefineMarkEventSerializer(competitorSerializer, new MarkJsonSerializer()));
    }

    private final JsonSerializer<RaceLogEvent> flagEventSerializer;
    private final JsonSerializer<RaceLogEvent> startTimeSerializer;
    private final JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private final JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> passChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer;
    private final JsonSerializer<RaceLogEvent> pathfinderEventSerializer;
    private final JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer;
    private final JsonSerializer<RaceLogEvent> startProcedureChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> protestStartTimeEventSerializer;
    private final JsonSerializer<RaceLogEvent> windFixEventSerializer;
    private final JsonSerializer<RaceLogEvent> deviceCompetitorMappingSerializer;
    private final JsonSerializer<RaceLogEvent> deviceMarkMappingSerializer;
    private final JsonSerializer<RaceLogEvent> denoteForTrackingSerializer;
    private final JsonSerializer<RaceLogEvent> startTrackingSerializer;
    private final JsonSerializer<RaceLogEvent> revokeSerializer;
    private final JsonSerializer<RaceLogEvent> registerCompetitorSerializer;
    private final JsonSerializer<RaceLogEvent> defineMarkSerializer;
    
    private JsonSerializer<RaceLogEvent> chosenSerializer;

    public RaceLogEventSerializer(
            JsonSerializer<RaceLogEvent> flagEventSerializer,
            JsonSerializer<RaceLogEvent> startTimeSerializer,
            JsonSerializer<RaceLogEvent> raceStatusSerializer,
            JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer,
            JsonSerializer<RaceLogEvent> passChangedEventSerializer,
            JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer, 
            JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer,
            JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer,
            JsonSerializer<RaceLogEvent> pathfinderEventSerializer,
            JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer,
            JsonSerializer<RaceLogEvent> startProcedureChangedEventSerializer,
            JsonSerializer<RaceLogEvent> protestStartTimeEventSerializer,
            JsonSerializer<RaceLogEvent> windFixEventSerializer,
            JsonSerializer<RaceLogEvent> deviceCompetitorMappingSerializer,
            JsonSerializer<RaceLogEvent> deviceMarkMappingSerializer,
            JsonSerializer<RaceLogEvent> denoteForTrackingSerializer,
            JsonSerializer<RaceLogEvent> createRaceSerializer,
            JsonSerializer<RaceLogEvent> revokeSerializer,
            JsonSerializer<RaceLogEvent> registerCompetitorSerializer,
            JsonSerializer<RaceLogEvent> defineMarkSerializer) {
        this.flagEventSerializer = flagEventSerializer;
        this.startTimeSerializer = startTimeSerializer;
        this.raceStatusSerializer = raceStatusSerializer;
        this.courseAreaChangedEventSerializer = courseAreaChangedEventSerializer;
        this.passChangedEventSerializer = passChangedEventSerializer;
        this.courseDesignChangedEventSerializer = courseDesignChangedEventSerializer;
        this.finishPositioningListChangedEventSerializer = finishPositioningListChangedEventSerializer;
        this.finishPositioningConfirmedEventSerializer = finishPositioningConfirmedEventSerializer;
        this.pathfinderEventSerializer = pathfinderEventSerializer;
        this.gateLineOpeningTimeEventSerializer = gateLineOpeningTimeEventSerializer;
        this.startProcedureChangedEventSerializer = startProcedureChangedEventSerializer;
        this.protestStartTimeEventSerializer = protestStartTimeEventSerializer;
        this.windFixEventSerializer = windFixEventSerializer;
        this.deviceCompetitorMappingSerializer = deviceCompetitorMappingSerializer;
        this.deviceMarkMappingSerializer = deviceMarkMappingSerializer;
        this.denoteForTrackingSerializer = denoteForTrackingSerializer;
        this.startTrackingSerializer = createRaceSerializer;
        this.revokeSerializer = revokeSerializer;
        this.registerCompetitorSerializer = registerCompetitorSerializer;
        this.defineMarkSerializer = defineMarkSerializer;
        
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
    public void visit(RaceLogCourseAreaChangedEvent event) {
        chosenSerializer = courseAreaChangedEventSerializer;
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
    public void visit(DeviceCompetitorMappingEvent event) {
        chosenSerializer = deviceCompetitorMappingSerializer;
    }

    @Override
    public void visit(DeviceMarkMappingEvent event) {
        chosenSerializer = deviceMarkMappingSerializer;
    }

    @Override
    public void visit(DenoteForTrackingEvent event) {
        chosenSerializer = denoteForTrackingSerializer;
    }
    @Override
    public void visit(StartTrackingEvent event) {
        chosenSerializer = startTrackingSerializer;
    }
    @Override
    public void visit(RevokeEvent event) {
        chosenSerializer = revokeSerializer;
    }
    @Override
    public void visit(RegisterCompetitorEvent event) {
        chosenSerializer = registerCompetitorSerializer;
    }

    @Override
    public void visit(DefineMarkEvent event) {
        chosenSerializer = defineMarkSerializer;
    }
}
