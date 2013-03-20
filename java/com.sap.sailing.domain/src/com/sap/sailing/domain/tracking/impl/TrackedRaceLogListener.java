package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
      //do nothing
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        trackedRace.onCourseDesignChangedByRaceCommittee(event.getCourseDesign());
    }

}
