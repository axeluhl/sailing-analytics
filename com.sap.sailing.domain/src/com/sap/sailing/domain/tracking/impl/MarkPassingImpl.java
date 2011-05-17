package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public class MarkPassingImpl implements MarkPassing {
    private final TimePoint timePoint;
    private final Waypoint waypoint;
    private final Competitor competitor;
    
    public MarkPassingImpl(TimePoint timePoint, Waypoint waypoint, Competitor competitor) {
        super();
        this.timePoint = timePoint;
        this.waypoint = waypoint;
        this.competitor = competitor;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

    @Override
    public Waypoint getWaypoint() {
        return waypoint;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

}
