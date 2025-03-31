package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateLeaderboardScoreCorrection extends AbstractLeaderboardScoreCorrectionUpdate {
    private static final long serialVersionUID = 697705655733594367L;
    private final Double correctedScore;
    
    /**
     * @param timePoint the time point for which to deliver leaderboard results as the result of this operation
     */
    public UpdateLeaderboardScoreCorrection(String leaderboardName, String columnName, String competitorIdAsString,
            Double correctedScore, TimePoint timePoint) {
        super(leaderboardName, columnName, competitorIdAsString, timePoint);
        this.correctedScore = correctedScore;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Double updateScoreCorrection(Leaderboard leaderboard, Competitor competitor, RaceColumn raceColumn) {
        final Double newTotalPoints;
        if (correctedScore == null) {
            leaderboard.getScoreCorrection().uncorrectScore(competitor, raceColumn);
            newTotalPoints = leaderboard.getTotalPoints(competitor, raceColumn, getTimePoint());
        } else {
            leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
            newTotalPoints = correctedScore;
        }
        return newTotalPoints;
    }
}
