package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasGPSFixContext {
    
    @Connector
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector
    public GPSFixMoving getGPSFix();
    
    @Dimension(messageKey="WindStrength", ordinal=10)
    public Cluster<Wind> getWindStrength();

}