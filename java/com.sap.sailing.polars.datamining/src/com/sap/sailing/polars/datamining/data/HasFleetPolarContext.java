package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.annotations.Connector;

public interface HasFleetPolarContext {
    
    @Connector(messageKey="Fleet")
    Fleet getFleet();
    
    RaceColumn getRaceColumn();
    
    TrackedRace getTrackedRace();
    
    @Connector(scanForStatistics=false)
    HasRaceColumnPolarContext getRaceColumnPolarContext();
    
}
