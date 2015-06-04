package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class StartTimeFinderResult {
    private final Iterable<SimpleRaceLogIdentifier> racesDependingOn;
    private final TimePoint startTime;

    public StartTimeFinderResult(final Iterable<SimpleRaceLogIdentifier> racesDependingOn, final TimePoint startTime) {
        this.racesDependingOn = racesDependingOn;
        this.startTime = startTime;
    }

    public StartTimeFinderResult(TimePoint startTime) {
        racesDependingOn = new ArrayList<SimpleRaceLogIdentifier>();
        this.startTime = startTime;
    }

    public StartTimeFinderResult(List<SimpleRaceLogIdentifier> racesDependingOn, TimePoint startTime) {
        this.racesDependingOn = racesDependingOn;
        this.startTime = startTime;
    }

    public Iterable<SimpleRaceLogIdentifier> getRacesDependingOn() {
        return racesDependingOn;
    }

    public TimePoint getStartTime() {
        return startTime;
    }

    public boolean isDependentStartTime() {
        if (racesDependingOn == null) {
            return false;
        }
        return Util.isEmpty(racesDependingOn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((racesDependingOn == null) ? 0 : racesDependingOn.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
        return true;
    }
}
