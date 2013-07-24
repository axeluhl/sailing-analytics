package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface SelectionContext {

    void setTrackedRace(TrackedRace trackedRace);

    void setCompetitor(Competitor competitor);

    void setTrackedLeg(TrackedLeg trackedLeg);

}
