package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface GPSFixContext {

    public TrackedRace getTrackedRace();

    public Competitor getCompetitor();

}