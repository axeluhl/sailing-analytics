package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;

public interface TrackedRace {
    RaceDefinition getRace();
    
    TimePoint getStart();
    
    TimePoint getFirstFinish();
    
    Iterable<TrackedLeg> getTrackedLegs();
    
    /**
     * Tracking information about the leg <code>competitor</code> is currently on, or
     * <code>null</code> if the competitor hasn't started any leg yet.
     */
    TrackedLegOfCompetitor getCurrentLeg(Competitor competitor);
    
    TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg);
    
    TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg);
    
    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    Track<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    /**
     * Tells the leg on which the <code>competitor</code> was at time <code>at</code>.
     * If the competitor hasn't passed the start waypoint yet, <code>null</code> is
     * returned because the competitor was not yet on any leg at that point in time. If
     * the time point happens to be after the last fix received from that competitor,
     * the last known leg for that competitor is returned. 
     */
    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at);
    
    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg);
    
    TimePoint getTimePointOfLastUpdate();
    
    int getRankDifference(Competitor competitor, Leg leg);
    
    int getRank(Competitor competitor, TimePoint timePoint);
    
    int getRank(Competitor competitor, Waypoint waypoint);
    
}
