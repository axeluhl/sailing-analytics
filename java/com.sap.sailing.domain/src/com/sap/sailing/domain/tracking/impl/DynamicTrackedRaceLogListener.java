package com.sap.sailing.domain.tracking.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
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
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.WindFixesFinder;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class DynamicTrackedRaceLogListener implements RaceLogEventVisitor {
    
    private static final Logger logger = Logger.getLogger(DynamicTrackedRaceLogListener.class.getName());

    private DynamicTrackedRace trackedRace;
    
    private final WindSource raceCommitteeWindSource;

    private RaceStatusAnalyzer statusAnalyzer;
    private LastPublishedCourseDesignFinder courseDesignFinder;
    private StartTimeFinder startTimeFinder;

    public DynamicTrackedRaceLogListener(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
        raceCommitteeWindSource = new WindSourceImpl(WindSourceType.RACECOMMITTEE);
    }

    public void addTo(RaceLog raceLog) {
        raceLog.addListener(this);
        trackedRace.invalidateStartTime();
        trackedRace.invalidateEndTime();
        courseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
        statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        startTimeFinder = new StartTimeFinder(raceLog);
        initializeWindTrack(raceLog);
        analyze();
    }

    /**
     * Retrieves all wind fixes available in the race log and adds them to the wind track for RACECOMMITTEEE
     * @param raceLog The race log from which the available wind fixes shall be retrieved.
     */
    private void initializeWindTrack(RaceLog raceLog) {
        WindFixesFinder windFixesFinder = new WindFixesFinder(raceLog);
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
            removeAllWindFixesFromWindTrack(raceLog);
            raceLog.removeListener(this);
        }
    }

    /**
     * Removes all wind fixes that are in the RaceLog from the RACECOMMITTEE wind source
     * @param raceLog the RaceLog of which the wind fixes shall be removed
     */
    private void removeAllWindFixesFromWindTrack(RaceLog raceLog) {
        WindFixesFinder windFixesFinder = new WindFixesFinder(raceLog);
        for (Wind wind : windFixesFinder.analyze()) {
            trackedRace.removeWind(wind, raceCommitteeWindSource);
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
        } else {
            logger.info("Could not find any course design update on race log of " + trackedRace.getRace().getName() + "! Not sending out any events.");
        }
    }
    
    private void analyzeStartTime() {
        /* start time will be set by StartTimeAnalyzer in TrackedRace.getStartTime() */
        trackedRace.invalidateStartTime();
        
        TimePoint startTime = startTimeFinder.analyze();
        if (startTime != null) {
            /* invoke listeners with received start time, this will also trigger tractrac update */
            trackedRace.onStartTimeChangedByRaceCommittee(startTime);
        } else {
            logger.info("Could not find any valid start time on race log of " + trackedRace.getRace().getName() + "! Not sending out any events.");
        }
    }
    
    @Override
    public void visit(RaceLogPassChangeEvent event) {
        trackedRace.invalidateStartTime();
        /* this will send tractrac the original start time */
        trackedRace.onStartTimeChangedByRaceCommittee(trackedRace.getStartTimeReceived());
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        analyzeStartTime();
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
        // add the wind fix to the race committee WindTrack
        trackedRace.recordWind(event.getWindFix(), raceCommitteeWindSource);
    }

}
