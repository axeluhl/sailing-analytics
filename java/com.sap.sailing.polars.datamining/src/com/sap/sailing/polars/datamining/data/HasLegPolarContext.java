package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface HasLegPolarContext {
    
    Leg getLeg();
    
    TrackedRace getTrackedRace();

}
