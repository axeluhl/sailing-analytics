package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;

public class MarkPassingImpl implements MarkPassing {
    private static final long serialVersionUID = -2673562761742403742L;
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
    
    @Override
    public String toString() {
        return ""+getTimePoint()+": "+getCompetitor()+" passed "+getWaypoint();
    }

    @Override
    public MarkPassing getOriginal() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitor == null) ? 0 : competitor.hashCode());
        result = prime * result + ((timePoint == null) ? 0 : timePoint.hashCode());
        result = prime * result + ((waypoint == null) ? 0 : waypoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MarkPassingImpl other = (MarkPassingImpl) obj;
        if (competitor == null) {
            if (other.competitor != null)
                return false;
        } else if (!competitor.equals(other.competitor))
            return false;
        if (timePoint == null) {
            if (other.timePoint != null)
                return false;
        } else if (!timePoint.equals(other.timePoint))
            return false;
        if (waypoint == null) {
            if (other.waypoint != null)
                return false;
        } else if (!waypoint.equals(other.waypoint))
            return false;
        return true;
    }

}
