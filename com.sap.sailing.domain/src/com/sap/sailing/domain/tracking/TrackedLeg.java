package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;

public interface TrackedLeg {
    Leg getLeg();
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    TrackedRace getTrackedRace();

}
