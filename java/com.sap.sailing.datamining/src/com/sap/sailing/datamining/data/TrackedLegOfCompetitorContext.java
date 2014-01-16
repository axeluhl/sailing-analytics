package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;

public interface TrackedLegOfCompetitorContext extends TrackedLegContext {

    public Competitor getCompetitor();

}