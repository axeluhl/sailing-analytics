package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPassingForOffsetWayPoint;

public class MarkPassingForOffsetImpl implements MarkPassingForOffsetWayPoint {

    private static final long serialVersionUID = -2168312586388285697L;
    private final TimePoint timePoint;
    private final Waypoint waypoint;
    private final Competitor competitor;
    private final MarkPassing markpassingforOffset;

    
    public MarkPassingForOffsetImpl(TimePoint timePoint, Waypoint waypoint, Competitor competitor, MarkPassing markPassingforOffset) {
        super();
        this.timePoint = timePoint;
        this.waypoint = waypoint;
        this.competitor = competitor;
        this.markpassingforOffset = markPassingforOffset;
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
    
    @Override
    public String toString() {
        return ""+getTimePoint()+": "+getCompetitor()+" passed "+getWaypoint();
    }

    @Override
    public MarkPassing getOffsetPassing() {
        
        return markpassingforOffset;
    }

}


   