package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Statistic;

public interface HasCompetitorPolarContext {
    
    @Connector(messageKey="Competitor")
    Competitor getCompetitor();
    
    TrackedRace getTrackedRace();
    
    Leg getLeg();
    
    @Statistic(messageKey="PolarData")
    PolarStatistic getPolarStatistics();

}
