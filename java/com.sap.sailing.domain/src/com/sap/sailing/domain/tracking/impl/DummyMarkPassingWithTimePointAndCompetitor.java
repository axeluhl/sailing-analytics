package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;

public class DummyMarkPassingWithTimePointAndCompetitor implements MarkPassing {
    private static final long serialVersionUID = -5494669910047887984L;
    private final TimePoint timePoint;
    private final Competitor competitor;
    
    public DummyMarkPassingWithTimePointAndCompetitor(TimePoint timePoint, Competitor competitor) {
        super();
        this.timePoint = timePoint;
        this.competitor = competitor;
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
        return competitor;
    }

    @Override
    public MarkPassing getOriginal() {
        return this;
    }
}
