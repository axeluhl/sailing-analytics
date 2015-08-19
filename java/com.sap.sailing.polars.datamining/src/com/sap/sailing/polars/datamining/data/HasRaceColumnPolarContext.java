package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasRaceColumnPolarContext {
    
    @Connector(messageKey="RaceColumn")
    RaceColumn getRaceColumn();

}
