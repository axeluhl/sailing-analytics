package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface GPSFixContext {

    public TrackedRace getTrackedRace();

    public int getLegNumber();

    public LegType getLegType();

    public Competitor getCompetitor();

}