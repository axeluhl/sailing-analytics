package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
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
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;

public class DynamicTrackedRaceLogListener implements RaceLogEventVisitor {

    private DynamicTrackedRace trackedRace;

    private StartTimeFinder startTimeFinder;

    private RaceStatusAnalyzer statusAnalyzer;

    private LastPublishedCourseDesignFinder courseDesignFinder;

    public DynamicTrackedRaceLogListener(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    public void addTo(RaceLog raceLog) {
        raceLog.addListener(this);

        startTimeFinder = new StartTimeFinder(raceLog);
        courseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
        statusAnalyzer = new RaceStatusAnalyzer(raceLog);

        analyze();
    }

    public void removeFrom(RaceLog raceLog) {
        // Gets called whenever a RaceColumn was already linked to a TrackedRace on linkage.
        // Maybe we need to reset the status of the old race somehow? There might be no new
        // TrackedRace to be linked...
        // Maybe something like this is needed:
        // TODO:
        // ??? trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.PREPARED, 0.0));
        raceLog.removeListener(this);
    }

    private void analyze() {
        analyzeStartTime();
        analyzeStatus();
        analyzeCourseDesign();
    }

    private void analyzeStartTime() {
        TimePoint startTime = startTimeFinder.getStartTime();

        // Because this code can be triggered by an obsolete (delayed) event or on an empty pass...
        // ... the current pass's start time might be null
        if (startTime != null) {
            // ... or setStartTimeReceived(TimePoint) might be called more than once with the same start time.
            trackedRace.setStartTimeReceived(startTime);
        } else {
            // TODO: Can we somehow withdraw a previously set start time?
        }
    }

    private void analyzeStatus() {
        /* RaceLogRaceStatus newStatus = */statusAnalyzer.getStatus();

        // TODO: What can we do with the status? Should we use DynamicTrackedRace.setStatus?
    }

    private void analyzeCourseDesign() {
        CourseBase courseDesign = courseDesignFinder.getLastCourseDesign();

        // On the initial analyze step after attaching the RaceLog there might be no course design.
        if (courseDesign != null) {
            // Because this code can be triggered by an obsolete (delayed) event...
            // ... onCourseDesignChangedByRaceCommittee() might be called more than once.
            trackedRace.onCourseDesignChangedByRaceCommittee(courseDesign);
        }
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        analyzeStartTime();
        analyzeStatus();
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        analyzeStartTime();
        analyzeStatus();
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        analyzeStatus();
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        analyzeCourseDesign();
    }

    @Override
    public void visit(RaceLogFlagEvent event) {

    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {

    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        // score correction is handled by the leaderboard
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        // score correction is handled by the leaderboard
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {

    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {

    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {

    }

}
