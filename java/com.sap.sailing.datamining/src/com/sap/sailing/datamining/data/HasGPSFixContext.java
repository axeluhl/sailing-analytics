package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasGPSFixContext {
    
    @Connector
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector
    public GPSFixMoving getGPSFix();

}