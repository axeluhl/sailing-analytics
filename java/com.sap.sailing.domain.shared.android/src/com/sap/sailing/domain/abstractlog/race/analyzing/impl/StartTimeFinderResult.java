package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.Collections;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class StartTimeFinderResult {
    public static enum ResolutionFailed { RACE_LOG_UNRESOLVED, CYCLIC_DEPENDENCY, NO_START_TIME_SET };
    
    private final Iterable<SimpleRaceLogIdentifier> dependingOnRaces;
    private final TimePoint startTime;
    
    /**
     * <code>null</code> if resolution worked
     */
    private final ResolutionFailed resolutionFailed;
    
    private Duration startTimeDiff;

    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime, Duration startTimeDiff) {
        this(racesDependingOn, startTime, startTimeDiff, /* resolutionFailed */ null);
    }
    
    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> racesDependingOn, Duration startTimeDiff, ResolutionFailed resolutionFailed) {
        this(racesDependingOn, /* startTime */ null, startTimeDiff, resolutionFailed);
    }
    
    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> dependingOnRaces, TimePoint startTime, Duration startTimeDiff,
            ResolutionFailed resolutionFailed) {
        this.startTime = startTime;
        this.startTimeDiff = startTimeDiff;
        this.dependingOnRaces = dependingOnRaces;
        this.resolutionFailed = resolutionFailed;
    }

    public StartTimeFinderResult(TimePoint startTime, Duration startTimeDiff) {
        this(/* racesDependingOn */ Collections.<SimpleRaceLogIdentifier>emptyList(), startTime, startTimeDiff);
    }

    public ResolutionFailed getResolutionFailed() {
        return resolutionFailed;
    }

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
