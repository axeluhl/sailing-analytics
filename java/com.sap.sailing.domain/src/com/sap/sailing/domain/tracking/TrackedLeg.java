package com.sap.sailing.domain.tracking;

import java.util.Map;

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

    /**
     * Computes the ranks of all competitors in this leg in one sweep. There are two advantages of this operation over
     * the more fine-grained {@link TrackedLegOfCompetitor#getRank(TimePoint)}:
     * <ol>
     * <li>all ranks are available in one call</li>
     * <li>no incoming events can influence ranks between separate calls to
     * {@link TrackedLegOfCompetitor#getRank(TimePoint)}</li>
     * </ol>
     */
    Map<Competitor, Integer> getRanks(TimePoint timePoint);

}
