package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasTrackedLegContext {
    
    @Connector
    public HasTrackedRaceContext getTrackedRaceContext();

    public TrackedLeg getTrackedLeg();
    
    @Dimension(messageKey="LegType", ordinal=6)
    public LegType getLegType();
    @Dimension(messageKey="LegNumber", ordinal=7)
    public int getLegNumber();

}