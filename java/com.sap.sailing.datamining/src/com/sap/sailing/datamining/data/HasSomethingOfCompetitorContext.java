package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;

public interface HasSomethingOfCompetitorContext {
    HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor", ordinal=2)
    Competitor getCompetitor();
    
    @Connector(messageKey="SailorProfile")
    default SailorProfile getSailorProfile() {
        return getTrackedRaceContext().getLeaderboardContext().getLeaderboardGroupContext().getSailorProfiles().getProfileForCompetitor(getCompetitor());
    }
}
