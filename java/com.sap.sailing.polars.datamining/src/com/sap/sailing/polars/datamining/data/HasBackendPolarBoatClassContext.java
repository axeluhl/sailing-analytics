package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.datamining.annotations.Connector;

public interface HasBackendPolarBoatClassContext {
    
    @Connector
    BoatClass getBoatClass();

}
