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
}
