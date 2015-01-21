package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasGPSFixContext {
    
    @Connector
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector
    public GPSFixMoving getGPSFix();
    
    @Dimension(messageKey="WindStrength", ordinal=10)
    public ClusterDTO getWindStrength();

}