package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.datamining.annotations.Connector;

public interface HasRaceColumnPolarContext {
    
    @Connector(messageKey="RaceColumn")
    RaceColumn getRaceColumn();
    
    @Connector(scanForStatistics=false)
    HasLeaderboardPolarContext getLeaderboardPolarContext();

}
