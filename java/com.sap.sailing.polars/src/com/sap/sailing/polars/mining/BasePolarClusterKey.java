package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface BasePolarClusterKey {

    @Dimension(messageKey = "boatClass")
    BoatClass getBoatClass();
    
}
