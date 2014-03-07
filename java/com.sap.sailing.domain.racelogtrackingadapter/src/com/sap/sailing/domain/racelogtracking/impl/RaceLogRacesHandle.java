package com.sap.sailing.domain.racelogtracking.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;

public class RaceLogRacesHandle implements RacesHandle {
    private final RaceLogRaceTracker tracker;

    public RaceLogRacesHandle(RaceLogRaceTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public Regatta getRegatta() {
        return tracker.getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return getRaces(-1);
    }

    @Override
    public Set<RaceDefinition> getRaces(long timeoutInMilliseconds) {
        long start = System.currentTimeMillis();
        Set<RaceDefinition> raceDefs = tracker.getRaces();

        RaceDefinition result = null;
        if (raceDefs != null && !raceDefs.isEmpty()) {
            result = tracker.getRaces().iterator().next();
        }
        boolean interrupted = false;
        synchronized (tracker) {
            while ((timeoutInMilliseconds == -1 || System.currentTimeMillis() - start < timeoutInMilliseconds)
                    && !interrupted && result == null) {
                try {
                    if (timeoutInMilliseconds == -1) {
                        tracker.wait();
                    } else {
                        long timeToWait = timeoutInMilliseconds - (System.currentTimeMillis() - start);
                        if (timeToWait > 0) {
                            tracker.wait(timeToWait);
                        }
                    }
                    result = tracker.getRaces().isEmpty() ? null : tracker.getRaces().iterator().next();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        return result == null ? null : Collections.singleton(result);
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return tracker.getTrackedRegatta();
    }

    @Override
    public RaceTracker getRaceTracker() {
        return tracker;
    }

}
