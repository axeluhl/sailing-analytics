package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * The result of a {@link StartTimeFinder} that tells about the dependencies through which the start time
 * may be resolved from other races, reasons for the failure to resolve a start time and the actual start
 * time as obtained immediately or transitively through a chain of dependencies.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StartTimeFinderResult {
    public static enum ResolutionFailed { RACE_LOG_UNRESOLVED, CYCLIC_DEPENDENCY, NO_START_TIME_SET };
    
    private final Iterable<SimpleRaceLogIdentifier> dependingOnRaces;
    private final TimePoint startTime;
    
    /**
     * <code>null</code> if resolution worked
     */
    private final ResolutionFailed resolutionFailed;
    
    private Duration startTimeDiff;
    
    /**
     * When a race log event provides the start time described by this result object then this field tells
     * the event's author. In particular, clients can see the priority from this. This may come in handy
     * when deriving other events from the "set start time" event, such as a tracking time event.
     */
    private AbstractLogEventAuthor startTimeEventAuthor;

    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime, Duration startTimeDiff, AbstractLogEventAuthor raceLogEventAuthor) {
        this(racesDependingOn, startTime, startTimeDiff, /* resolutionFailed */ null, raceLogEventAuthor);
    }
    
    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> racesDependingOn, Duration startTimeDiff, ResolutionFailed resolutionFailed, AbstractLogEventAuthor raceLogEventAuthor) {
        this(racesDependingOn, /* startTime */ null, startTimeDiff, resolutionFailed, raceLogEventAuthor);
    }
    
    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> dependingOnRaces, TimePoint startTime, Duration startTimeDiff,
            ResolutionFailed resolutionFailed, AbstractLogEventAuthor raceLogEventAuthor) {
        this.startTime = startTime;
        this.startTimeDiff = startTimeDiff;
        this.dependingOnRaces = dependingOnRaces;
        this.resolutionFailed = resolutionFailed;
    }

    public StartTimeFinderResult(TimePoint startTime, Duration startTimeDiff, AbstractLogEventAuthor raceLogEventAuthor) {
        this(/* racesDependingOn */ Collections.<SimpleRaceLogIdentifier>emptyList(), startTime, startTimeDiff, raceLogEventAuthor);
    }

    /**
     * When a race log event provides the start time described by this result object then this field tells
     * the event's author. In particular, clients can see the priority from this. This may come in handy
     * when deriving other events from the "set start time" event, such as a tracking time event.
     * 
     * @return {@code null} in case no author is known, e.g., if no start time was set in the race log
     */
    public AbstractLogEventAuthor geStartTimeEventAuthor() {
        return startTimeEventAuthor;
    }
    
    /**
     * @return {@code null} if the start time was resolved; a reason for failure to resolve otherwise
     */
    public ResolutionFailed getResolutionFailed() {
        return resolutionFailed;
    }

    /**
     * @return the chain of races on which this race's start time depends; the last entry will have an absolute start
     *         time; the first one is the one on which this race's start time depends immediately
     */
    public Iterable<SimpleRaceLogIdentifier> getDependingOnRaces() {
        return dependingOnRaces;
    }

    public TimePoint getStartTime() {
        return startTime;
    }

    public Duration getStartTimeDiff() {
        return startTimeDiff;
    }

    public void setStartTimeDiff(Duration startTimeDiff) {
        this.startTimeDiff = startTimeDiff;
    }

    public boolean isDependentStartTime() {
        return dependingOnRaces != null && !Util.isEmpty(dependingOnRaces);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dependingOnRaces == null) ? 0 : dependingOnRaces.hashCode());
        result = prime * result + ((resolutionFailed == null) ? 0 : resolutionFailed.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((startTimeDiff == null) ? 0 : startTimeDiff.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StartTimeFinderResult other = (StartTimeFinderResult) obj;
        if (dependingOnRaces == null) {
            if (other.dependingOnRaces != null)
                return false;
        } else if (!dependingOnRaces.equals(other.dependingOnRaces))
            return false;
        if (resolutionFailed != other.resolutionFailed)
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        if (startTimeDiff == null) {
            if (other.startTimeDiff != null)
                return false;
        } else if (!startTimeDiff.equals(other.startTimeDiff))
            return false;
        return true;
    }
}
