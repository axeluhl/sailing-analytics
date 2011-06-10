package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.TimePoint;

public interface TrackedLeg {
    Leg getLeg();
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    TrackedRace getTrackedRace();

    /**
     * Determines whether the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the current wind's bearing.
     */
    boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException;

}
