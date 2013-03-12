package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.RaceLogEventSerializerChooser;

public class RaceLogEventSerializerChooserImpl implements RaceLogEventSerializerChooser, RaceLogEventVisitor  {

    private final JsonSerializer<RaceLogEvent> flagEventSerializer;
    private final JsonSerializer<RaceLogEvent> startTimeSerializer;
    private final JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private final JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
    private final JsonSerializer<RaceLogEvent> passChangedEventSerializer;

    private JsonSerializer<RaceLogEvent> chosenSerializer;

    public RaceLogEventSerializerChooserImpl(
            JsonSerializer<RaceLogEvent> flagEventSerializer,
            JsonSerializer<RaceLogEvent> startTimeSerializer,
            JsonSerializer<RaceLogEvent> raceStatusSerializer,
            JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer,
            JsonSerializer<RaceLogEvent> passChangedEventSerializer) {
        this.flagEventSerializer = flagEventSerializer;
        this.startTimeSerializer = startTimeSerializer;
        this.raceStatusSerializer = raceStatusSerializer;
        this.courseAreaChangedEventSerializer = courseAreaChangedEventSerializer;
        this.passChangedEventSerializer = passChangedEventSerializer;

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
        // TODO Auto-generated method stub
        
    }

}
