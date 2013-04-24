package com.sap.sailing.server.gateway.serialization.racelog.impl;

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
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.RaceLogEventSerializerChooser;

public class RaceLogEventSerializerChooserImpl implements RaceLogEventSerializerChooser, RaceLogEventVisitor  {

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

    private JsonSerializer<RaceLogEvent> chosenSerializer;

    public RaceLogEventSerializerChooserImpl(
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
            JsonSerializer<RaceLogEvent> startProcedureChangedEventSerializer) {
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
        
        this.chosenSerializer = null;
    }

    @Override
    public JsonSerializer<RaceLogEvent> getSerializer(RaceLogEvent event) {
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

}
