package com.sap.sailing.domain.leaderboard.meta;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class MetaLeaderboardScoreCorrection extends ScoreCorrectionImpl {
    private static final long serialVersionUID = 3773423384260065869L;

    public MetaLeaderboardScoreCorrection(MetaLeaderboard leaderboard) {
        super(leaderboard);
    }
    
    @Override
    protected Integer getNumberOfCompetitorsInRace(RaceColumn raceColumn, Competitor competitor,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        MetaLeaderboardColumn metaLeaderboardColumn = (MetaLeaderboardColumn) raceColumn;
        return Util.size(metaLeaderboardColumn.getLeaderboard().getCompetitors());
    }

    /**
     * These redefinitions are required for scoping reasons, to make the method visible also to other classes in this package.
     */
    @Override
    protected void notifyListeners(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore) {
        super.notifyListeners(competitor, raceColumn, oldCorrectedScore, newCorrectedScore);
    }

    /**
     * These redefinitions are required for scoping reasons, to make the method visible also to other classes in this package.
     */
    @Override
    protected void notifyListeners(Competitor competitor, RaceColumn raceColumn,
            MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason) {
        super.notifyListeners(competitor, raceColumn, oldMaxPointsReason, newMaxPointsReason);
    }
    
    @Override
    protected MetaLeaderboard getLeaderboard() {
        return (MetaLeaderboard) super.getLeaderboard();
    }

    @Override
    public TimePoint getTimePointOfLastCorrectionsValidity() {
        TimePoint result = super.getTimePointOfLastCorrectionsValidity();
        for (Leaderboard leaderboard : getLeaderboard().getLeaderboards()) {
            TimePoint leaderboardLastCorrectionTimePoint = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
            if (result == null || (leaderboardLastCorrectionTimePoint != null && leaderboardLastCorrectionTimePoint.after(result))) {
                result = leaderboardLastCorrectionTimePoint;
            }
        }
        return result;
    }
}
