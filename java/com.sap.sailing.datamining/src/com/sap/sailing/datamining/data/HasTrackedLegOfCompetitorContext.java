package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasTrackedLegOfCompetitorContext extends HasTrackedLegContext {

    @SideEffectFreeValue(messageKey="Competitor")
    public Competitor getCompetitor();

}