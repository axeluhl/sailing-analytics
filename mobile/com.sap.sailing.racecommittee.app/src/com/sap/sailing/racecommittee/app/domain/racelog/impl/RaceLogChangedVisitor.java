package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.racecommittee.app.domain.racelog.RaceLogChangedListener;

public class RaceLogChangedVisitor implements RaceLogEventVisitor {

    private RaceLogChangedListener listener;

    public RaceLogChangedVisitor(RaceLogChangedListener listener) {
        this.listener = listener;
    }

    public void visit(RaceLogFlagEvent event) {
        listener.eventAdded(event);
    }

    public void visit(RaceLogPassChangeEvent event) {
        listener.eventAdded(event);
    }

    public void visit(RaceLogRaceStatusEvent event) {
        listener.eventAdded(event);
    }

    public void visit(RaceLogStartTimeEvent event) {
        listener.eventAdded(event);
    }

    public void visit(RaceLogCourseAreaChangedEvent event) {
        listener.eventAdded(event);
    }

    public void visit(RaceLogCourseDesignChangedEvent event) {
        listener.eventAdded(event);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        listener.eventAdded(event);
    }

}
