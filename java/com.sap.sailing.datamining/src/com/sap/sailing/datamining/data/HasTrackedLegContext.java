package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasTrackedLegContext extends HasTrackedRaceContext {

    public TrackedLeg getTrackedLeg();
    
    @Dimension(messageKey="LegType", ordinal=5)
    public LegType getLegType();
    @Dimension(messageKey="LegNumber", ordinal=6)
    public int getLegNumber();

}