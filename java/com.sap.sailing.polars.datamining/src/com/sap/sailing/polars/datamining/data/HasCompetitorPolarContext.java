package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasCompetitorPolarContext {
    
    @Connector(messageKey="Competitor")
    Competitor getCompetitor();
    
    TrackedRace getTrackedRace();
    
    Leg getLeg();
    
    TrackedLegOfCompetitor getTrackedLegOfCompetitor();

}
