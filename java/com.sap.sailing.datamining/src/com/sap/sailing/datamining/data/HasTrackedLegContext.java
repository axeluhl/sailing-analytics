package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedLeg;

public interface HasTrackedLegContext extends HasTrackedRaceContext {

    public TrackedLeg getTrackedLeg();
    public LegType getLegType();
    public int getLegNumber();

}