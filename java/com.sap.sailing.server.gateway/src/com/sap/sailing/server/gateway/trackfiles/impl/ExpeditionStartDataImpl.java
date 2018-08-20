package com.sap.sailing.server.gateway.trackfiles.impl;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;

public class ExpeditionStartDataImpl implements ExpeditionStartData {
    private final Iterable<GPSFix> startLinePortFixes;
    private final Iterable<GPSFix> startLineStarboardFixes;
    private final Iterable<TimePoint> startTimes;
    
    public ExpeditionStartDataImpl(Iterable<GPSFix> startLinePortFixes, Iterable<GPSFix> startLineStarboardFixes, Iterable<TimePoint> startTimes) {
        super();
        this.startLinePortFixes = startLinePortFixes;
        this.startLineStarboardFixes = startLineStarboardFixes;
        this.startTimes = startTimes;
    }
    
    @Override
    public Iterable<GPSFix> getStartLinePortFixes() {
        return startLinePortFixes;
    }

    @Override
    public Iterable<GPSFix> getStartLineStarboardFixes() {
        return startLineStarboardFixes;
    }

    @Override
    public Iterable<TimePoint> getStartTimes() {
        return startTimes;
    }

}
