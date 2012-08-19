package com.sap.sailing.domain.leaderboard.meta;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;

public class MetaLeaderboardScoreCorrection extends ScoreCorrectionImpl {
    private static final long serialVersionUID = 3773423384260065869L;
    private AbstractMetaLeaderboard metaLeaderboard;

    public MetaLeaderboardScoreCorrection() {
    }
    
    protected void setMetaLeaderboard(AbstractMetaLeaderboard metaLeaderboard) {
        this.metaLeaderboard = metaLeaderboard;
    }
    
    @Override
    protected Integer getNumberOfCompetitorsInRace(RaceColumn raceColumn, Competitor competitor,
            int numberOfCompetitorsInLeaderboard) {
        MetaLeaderboardColumn metaLeaderboardColumn = (MetaLeaderboardColumn) raceColumn;
        return Util.size(metaLeaderboardColumn.getLeaderboard().getCompetitors());
    }

    @Override
    protected void notifyListeners(Competitor competitor, Double oldCorrectedScore, Double newCorrectedScore) {
        super.notifyListeners(competitor, oldCorrectedScore, newCorrectedScore);
    }

    @Override
    protected void notifyListeners(Competitor competitor, MaxPointsReason oldMaxPointsReason,
            MaxPointsReason newMaxPointsReason) {
        super.notifyListeners(competitor, oldMaxPointsReason, newMaxPointsReason);
    }

    @Override
    public TimePoint getTimePointOfLastCorrectionsValidity() {
        TimePoint result = super.getTimePointOfLastCorrectionsValidity();
        for (Leaderboard leaderboard : metaLeaderboard.getLeaderboards()) {
            TimePoint leaderboardLastCorrectionTimePoint = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
            if (result == null || (leaderboardLastCorrectionTimePoint != null && leaderboardLastCorrectionTimePoint.after(result))) {
                result = leaderboardLastCorrectionTimePoint;
            }
        }
        return result;
    }
}
