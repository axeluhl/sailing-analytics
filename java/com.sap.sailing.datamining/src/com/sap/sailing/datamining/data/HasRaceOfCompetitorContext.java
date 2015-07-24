package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.datamining.shared.data.Unit;

public interface HasRaceOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Statistic(messageKey="DistanceAtStart", resultUnit=Unit.Meters, resultDecimals=0, ordinal=0)
    public double getDistanceToStartLineAtStart();
    
    @Connector(messageKey="SpeedAtStart", ordinal=1)
    public Speed getSpeedAtStart();
    
    @Connector(messageKey="SpeedTenSecondsBeforeStart", ordinal=2)
    public Speed getSpeedTenSecondsBeforeStart();
    
}
