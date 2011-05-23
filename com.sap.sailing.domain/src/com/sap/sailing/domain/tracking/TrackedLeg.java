package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;

public interface TrackedLeg {
    Leg getLeg();
    
    /**
     * Computes the competitor's rank within this leg. If the competitor has already finished this leg, the rank is
     * determined by comparing to all other competitors that also finished this leg. If not yet finished, the rank is
     * i+j+1 where i is the number of competitors that already finished the leg, and j is the number of competitors
     * whose wind-projected distance to the leg's end waypoint is shorter than that of <code>competitor</code>.
     * <p>
     * 
     * The wind projection is only an approximation of a more exace "advantage line" and in particular doesn't
     * account for crossing the lay line.
     */
    int getRank(Competitor competitor);
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    int getRankAtBeginningOfLeg(Competitor competitor);

    int getRankAtEndOfLeg(Competitor competitor);

}
