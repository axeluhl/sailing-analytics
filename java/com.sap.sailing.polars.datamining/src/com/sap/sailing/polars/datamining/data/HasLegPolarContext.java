package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasLegPolarContext {
    
    Leg getLeg();
    
    TrackedRace getTrackedRace();
    
    @Dimension(messageKey="LegIndex")
    Integer getLegIndex();
    
    @Connector(scanForStatistics=false)
    HasFleetPolarContext getFleetPolarContext();

}
