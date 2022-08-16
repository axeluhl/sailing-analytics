package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasBackendPolarBoatClassContext {
    
    @Connector(messageKey="BoatClass")
    BoatClass getBoatClass();

    PolarDataService getPolarDataService();
    
    @Statistic(messageKey="BackendPolars")
    HasBackendPolarBoatClassContext getBackendPolarBoatClassContext();

    @Statistic(messageKey="TargetBeatAngle")
    Double getTargetBeatAngle();

    @Statistic(messageKey="TargetRunawayAngle")
    Double getTargetRunawayAngle();

}
