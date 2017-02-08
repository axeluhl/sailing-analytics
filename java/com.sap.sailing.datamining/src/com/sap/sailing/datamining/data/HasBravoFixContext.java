package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasBravoFixContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    BravoFix getBravoFix();

    @Dimension(messageKey="IsFoiling")
    boolean isFoiling();
    
    @Override
    default TimePoint getTimePoint() {
        return getBravoFix().getTimePoint();
    }
}