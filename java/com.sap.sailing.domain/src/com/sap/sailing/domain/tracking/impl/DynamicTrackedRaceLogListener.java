package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
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
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.WindFixesFinder;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class DynamicTrackedRaceLogListener implements RaceLogEventVisitor {

    private DynamicTrackedRace trackedRace;
    
    private WindSource raceCommitteeWindSource;

    private RaceStatusAnalyzer statusAnalyzer;

    private LastPublishedCourseDesignFinder courseDesignFinder;

    public DynamicTrackedRaceLogListener(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    public void addTo(RaceLog raceLog) {
        raceLog.addListener(this);
        trackedRace.invalidateStartTime();
        trackedRace.invalidateEndTime();
        courseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
        statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        initializeWindTrack(raceLog);
        analyze();
    }

    /**
     * Creates a wind track for the source type RACECOMMITTEE in tracked race, retrieves all wind fixes available in the race log and adds them to the wind track
     * @param raceLog The race log from which the available wind fixes shall be retrieved.
     */
    private void initializeWindTrack(RaceLog raceLog) {
        WindFixesFinder windFixesFinder = new WindFixesFinder(raceLog);
        raceCommitteeWindSource = new WindSourceImpl(WindSourceType.RACECOMMITTEE);
        trackedRace.getOrCreateWindTrack(raceCommitteeWindSource);
        for (Wind wind : windFixesFinder.analyze()) {
            trackedRace.recordWind(wind, raceCommitteeWindSource);
        }
    }

    public void removeFrom(RaceLog raceLog) {
        // Gets called whenever a RaceColumn was already linked to a TrackedRace on linkage.
        // Maybe we need to reset the status of the old race somehow? There might be no new
        // TrackedRace to be linked...
        // Maybe something like this is needed:
        // TODO:
        // ??? trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.PREPARED, 0.0));
        if (raceLog != null) {
            trackedRace.invalidateStartTime();
            //TODO detach wind source
            raceLog.removeListener(this);
        }
    }

    private void analyze() {
        analyzeStatus();
        analyzeCourseDesign();
    }

    private void analyzeStatus() {
        /* RaceLogRaceStatus newStatus = */statusAnalyzer.analyze();

        // TODO: What can we do with the status? Should we use DynamicTrackedRace.setStatus?
    }

    private void analyzeCourseDesign() {
        CourseBase courseDesign = courseDesignFinder.analyze();

        // On the initial analyze step after attaching the RaceLog there might be no course design.
        if (courseDesign != null) {
            // Because this code can be triggered by an obsolete (delayed) event...
            // ... onCourseDesignChangedByRaceCommittee() might be called more than once.
            trackedRace.onCourseDesignChangedByRaceCommittee(courseDesign);
        }
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        trackedRace.invalidateStartTime();
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        trackedRace.invalidateStartTime();
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

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        // TODO handle a new wind fix entered by the race committee
        // add the wind fix to the race committee WindTrack
        trackedRace.recordWind(event.getWindFix(), raceCommitteeWindSource);
    }

}
