package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public class MarkPassingImpl implements MarkPassing {
    private static final long serialVersionUID = -2673562761742403742L;
    private final TimePoint timePoint;
    private final Waypoint waypoint;
    private final Competitor competitor;
    private final Mark mark;
    
    public MarkPassingImpl(TimePoint timePoint, Waypoint waypoint, Competitor competitor) {
        this(timePoint, waypoint, null, competitor);
    }
    
    public MarkPassingImpl(TimePoint timePoint, Waypoint waypoint, Mark mark, Competitor competitor) {
    	super();
    	this.timePoint = timePoint;
        this.waypoint = waypoint;
        this.competitor = competitor;
    	this.mark = mark;
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
    public Mark getMark() {
		return mark;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public String toString() {
        return ""+getTimePoint()+": "+getCompetitor()+" passed "+getWaypoint();
    }

}
