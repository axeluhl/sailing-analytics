package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasTrackedLegOfCompetitorContext extends HasTrackedLegContext {
    
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor();

    @SideEffectFreeValue(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @SideEffectFreeValue(messageKey="DistanceTraveled", resultUnit=Unit.Meters, resultDecimals=0)
    public Double getDistanceTraveled();

}