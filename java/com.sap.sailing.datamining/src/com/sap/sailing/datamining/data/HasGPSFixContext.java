package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.datamining.annotations.Connector;

public interface HasGPSFixContext extends HasWind {
    
    @Connector(scanForStatistics=false)
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    public GPSFixMoving getGPSFix();
    
}