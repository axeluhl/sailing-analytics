package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;

public class RaceLogRacesHandle implements RaceHandle {
    private final RaceLogRaceTracker tracker;

    public RaceLogRacesHandle(RaceLogRaceTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public Regatta getRegatta() {
        return tracker.getRegatta();
    }

    @Override
    public RaceDefinition getRace() {
        return getRace(-1);
    }

    @Override
    public RaceDefinition getRace(long timeoutInMilliseconds) {
        long start = System.currentTimeMillis();
        RaceDefinition result = tracker.getRace();
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
                    result = tracker.getRace();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        return result;
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
