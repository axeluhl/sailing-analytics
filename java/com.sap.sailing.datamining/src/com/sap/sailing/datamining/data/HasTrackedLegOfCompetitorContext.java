package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;

public interface HasTrackedLegOfCompetitorContext extends HasTrackedLegContext {

    public Competitor getCompetitor();

}