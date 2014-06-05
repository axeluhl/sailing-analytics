package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasGPSFixContext extends HasTrackedLegOfCompetitorContext {
    
    @Connector
    public GPSFixMoving getGPSFix();

}