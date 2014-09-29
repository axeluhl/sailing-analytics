package com.sap.sailing.datamining.factories;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

public enum SailingDataRetrievalLevels {
    
    LeaderboardGroup(LeaderboardGroup.class),
    RegattaLeaderboard(RegattaLeaderboard.class),
    TrackedRace(HasTrackedRaceContext.class),
    TrackedLeg(HasTrackedLegContext.class),
    TrackedLegOfCompetitor(HasTrackedLegOfCompetitorContext.class),
    GPSFix(HasGPSFixContext.class);
    
    private Class<?> dataType;
    
    private SailingDataRetrievalLevels(Class<?> dataType) {
        this.dataType = dataType;
    }

    public Class<?> getDataType() {
        return dataType;
    }
    
}
