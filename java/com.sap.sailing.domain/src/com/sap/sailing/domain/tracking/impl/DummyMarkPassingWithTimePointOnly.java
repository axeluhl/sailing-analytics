package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public class DummyMarkPassingWithTimePointOnly implements MarkPassing {
    private final TimePoint timePoint;
    
    public DummyMarkPassingWithTimePointOnly(TimePoint timePoint) {
        super();
        this.timePoint = timePoint;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

    @Override
    public Waypoint getWaypoint() {
        throw new UnsupportedOperationException("getWaypoint() not supported");
    }

    @Override
    public Competitor getCompetitor() {
        throw new UnsupportedOperationException("getCompetitor() not supported");
    }

}
