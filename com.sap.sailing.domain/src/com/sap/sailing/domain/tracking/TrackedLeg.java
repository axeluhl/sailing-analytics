package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;

public interface TrackedLeg {
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfBoats();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    int getRankAtBeginningOfLeg(Competitor competitor);

    int getRankAtEndOfLeg(Competitor competitor);

    int getRankDifference(Competitor competitor);
}
