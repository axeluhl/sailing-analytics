package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;

public interface HasBravoFixContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    public BravoFix getBravoFix();

    @Override
    default TimePoint getTimePoint() {
        return getBravoFix().getTimePoint();
    }
}