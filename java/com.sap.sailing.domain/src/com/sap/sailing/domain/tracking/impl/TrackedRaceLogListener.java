package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
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
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceLogListener implements RaceLogEventVisitor {

    private TrackedRace trackedRace;

    public TrackedRaceLogListener(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
        //do nothing
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        //do nothing
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        ((DynamicTrackedRace) trackedRace).setStartTimeReceived(event.getStartTime());
    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        //do nothing
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        trackedRace.onCourseDesignChangedByRaceCommittee(event.getCourseDesign());
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        // do nothing (wait for RaceLogFinishPositioningConfirmedEvent to perform score corrections)
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
     // do nothing score correction is handled by the leaderboard
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        // do nothing
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        // do nothing
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        // do nothing
    }

}
