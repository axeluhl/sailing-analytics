package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;

public class RaceResultOfCompetitorWithContext implements HasRaceResultOfCompetitorContext {

    private final HasLeaderboardContext leaderboardWithContext;
    private final RaceColumn raceColumn;
    private final Competitor competitor;

    public RaceResultOfCompetitorWithContext(HasLeaderboardContext leaderboardWithContext, RaceColumn raceColumn, Competitor competitor) {
        this.leaderboardWithContext = leaderboardWithContext;
        this.raceColumn = raceColumn;
        this.competitor = competitor;
    }

    @Override
    public HasLeaderboardContext getLeaderboardContext() {
        return leaderboardWithContext;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public double getRank() {
//        TrackedRace trackedRace = getLeaderboardContext().getTrackedRace();
//        RaceColumn raceColumn = null;
//        Leaderboard leaderboard = getLeaderboardContext().getLeaderboard();
//        double competitorAmount = 0;
//        try {
//            double rank = leaderboard.getNetPoints(competitor, raceColumn, trackedRace.getEndOfRace());
//            return rank / competitorAmount;
//        } catch (NoWindException e) {
//            return 0;
//        }
        return 0;
    }
    
}
