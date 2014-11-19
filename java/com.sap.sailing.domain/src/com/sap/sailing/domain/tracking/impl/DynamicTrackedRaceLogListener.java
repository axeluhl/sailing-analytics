package com.sap.sailing.domain.tracking.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.WindFixesFinder;
import com.sap.sailing.domain.racelog.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

/**
 * TODO: this class could be a good place to leverage more information about a race containing in the {@link RaceLog}.
 * This includes for example the {@link RaceLogRaceStatus} indicating the current race's start.
 */
public class DynamicTrackedRaceLogListener extends BaseRaceLogEventVisitor {
    
    private static final Logger logger = Logger.getLogger(DynamicTrackedRaceLogListener.class.getName());

    private DynamicTrackedRace trackedRace;
    
    private final WindSource raceCommitteeWindSource;

    private LastPublishedCourseDesignFinder courseDesignFinder;
    private StartTimeFinder startTimeFinder;
    private AbortingFlagFinder abortingFlagFinder;

    public DynamicTrackedRaceLogListener(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
        raceCommitteeWindSource = new WindSourceImpl(WindSourceType.RACECOMMITTEE);
    }

    public void addTo(RaceLog raceLog) {
        if (raceLog == null) {
            logger.severe("Trying to add "+this+" as listener to a null race log for tracked race "+trackedRace.getRace());
        } else {
            raceLog.addListener(this);
            trackedRace.invalidateStartTime();
            trackedRace.invalidateEndTime();
            courseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
            startTimeFinder = new StartTimeFinder(raceLog);
            abortingFlagFinder = new AbortingFlagFinder(raceLog);
            initializeWindTrack(raceLog);
            analyze();
        }
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
        analyzeCourseDesign(null);
    }

    private void analyzeCourseDesign(CourseBase courseBaseProvidedByEvent) {
        CourseBase courseDesign = courseDesignFinder.analyze();
        if (courseDesign == null) {
            courseDesign = courseBaseProvidedByEvent;
        }

        // On the initial analyze step after attaching the RaceLog there might be no course design.
        if (courseDesign != null) {
            // Because this code can be triggered by an obsolete (delayed) event...
            // ... onCourseDesignChangedByRaceCommittee() might be called more than once.
            trackedRace.onCourseDesignChangedByRaceCommittee(courseDesign);
        } else {
            logger.info("Could not find any course design update on race log of " + trackedRace.getRace().getName() + "! Not sending out any events.");
        }
    }
    
    private void analyzeStartTime(TimePoint startTimeProvidedByEvent) {
        /* start time will be set by StartTimeFinder in TrackedRace.getStartTime() */
        trackedRace.invalidateStartTime();
        
        TimePoint startTime = startTimeFinder.analyze();
        if (startTime == null) {
            startTime = startTimeProvidedByEvent;
        }
        
        if (startTime != null) {
            /* invoke listeners with received start time, this will also trigger tractrac update */
            trackedRace.onStartTimeChangedByRaceCommittee(startTime);
        } else {
            logger.info("Could not find any valid start time on race log of " + trackedRace.getRace().getName() + "! Not sending out any events.");
        }
    }
    
    @Override
    public void visit(RaceLogPassChangeEvent event) {
        trackedRace.invalidateStartTime(); // this will notify RaceStateListeners in case the start time changes by the event
        /* reset start time */
        trackedRace.onStartTimeChangedByRaceCommittee(null);
        RaceLogFlagEvent abortingFlag = abortingFlagFinder.analyze();
        if (abortingFlag != null) {
            // previous pass was aborted; notify TracTrac
            trackedRace.onAbortedByRaceCommittee(abortingFlag.getUpperFlag());
        }
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        analyzeStartTime(event.getStartTime());
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        analyzeCourseDesign(event.getCourseDesign());
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        // add the wind fix to the race committee WindTrack
        trackedRace.recordWind(event.getWindFix(), raceCommitteeWindSource);
    }
}
