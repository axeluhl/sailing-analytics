package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.datamining.shared.data.Unit;

public interface HasTrackedLegOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasTrackedLegContext getTrackedLegContext();
    
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Statistic(messageKey="DistanceTraveled", resultUnit=Unit.Meters, resultDecimals=0, ordinal=0)
    public Double getDistanceTraveled();

}