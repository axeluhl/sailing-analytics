package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class StartTimeFinderResult {
    private Iterable<SimpleRaceLogIdentifier> racesDependingOn;
    private TimePoint startTime;
    private Duration startTimeDiff;

    public StartTimeFinderResult(Iterable<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime, Duration startTimeDiff) {
        this(startTime, startTimeDiff);
        this.racesDependingOn = racesDependingOn;
    }

    public StartTimeFinderResult(TimePoint startTime, Duration startTimeDiff) {
        racesDependingOn = new ArrayList<>();
        this.startTime = startTime;
        this.startTimeDiff = startTimeDiff;
    }

    public StartTimeFinderResult(List<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime, Duration startTimeDiff) {
        this(startTime, startTimeDiff);
        this.racesDependingOn = racesDependingOn;
    }

    public Iterable<SimpleRaceLogIdentifier> getRacesDependingOn() {
        return racesDependingOn;
    }

    public TimePoint getStartTime() {
        return startTime;
    }

    public Duration getStartTimeDiff() {
        return startTimeDiff;
    }

    public boolean isDependentStartTime() {
        return racesDependingOn != null && !Util.isEmpty(racesDependingOn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((racesDependingOn == null) ? 0 : racesDependingOn.hashCode());
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
        if (racesDependingOn == null) {
            if (other.racesDependingOn != null)
                return false;
        } else if (!racesDependingOn.equals(other.racesDependingOn))
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
