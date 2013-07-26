package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public interface SelectionContext {

    public TrackedLeg getTrackedLeg();
    public Competitor getCompetitor();
    public TrackedRace getTrackedRace();
    public TrackedRegatta getTrackedRegatta();

}
