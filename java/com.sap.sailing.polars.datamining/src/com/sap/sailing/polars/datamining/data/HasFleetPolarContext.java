package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasFleetPolarContext {
    
    @Connector(messageKey="Fleet")
    Fleet getFleet();
    
    RaceColumn getRaceColumn();
    
    TrackedRace getTrackedRace();
    
    @Connector(messageKey="BoatClass")
    BoatClass getBoatClass();
    
}
