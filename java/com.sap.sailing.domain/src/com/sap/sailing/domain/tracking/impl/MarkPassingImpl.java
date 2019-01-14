package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;

public class MarkPassingImpl implements MarkPassing {
    private static final long serialVersionUID = -2673562761742403742L;
    private final TimePoint timePoint;
    private final Waypoint waypoint;
    private final Competitor competitor;
    private final Candidate candidate;
    
    public MarkPassingImpl(TimePoint timePoint, Waypoint waypoint, Competitor competitor, Candidate candidate) {
    	super();
    	this.timePoint = timePoint;
        this.waypoint = waypoint;
        this.competitor = competitor;
        this.candidate = candidate;
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
    public MarkPassing getOriginal() {
        return this;
    }
    
    public Candidate getCandidate() {
        return this.candidate;
    }

}
