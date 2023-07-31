package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateLeaderboardIncrementalScoreCorrection extends AbstractLeaderboardScoreCorrectionUpdate {
    private static final long serialVersionUID = 9064307077124881657L;
    private final Double scoreOffsetInPoints;
    
    /**
     * @param timePoint the time point for which to deliver leaderboard results as the result of this operation
     */
    public UpdateLeaderboardIncrementalScoreCorrection(String leaderboardName, String columnName, String competitorIdAsString,
            Double scoreOffsetInPoints, TimePoint timePoint) {
        super(leaderboardName, columnName, competitorIdAsString, timePoint);
        this.scoreOffsetInPoints = scoreOffsetInPoints;
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
        if (scoreOffsetInPoints == null) {
            leaderboard.getScoreCorrection().uncorrectScoreIncrementally(competitor, raceColumn);
        } else {
            leaderboard.getScoreCorrection().correctScoreIncrementally(competitor, raceColumn, scoreOffsetInPoints);
        }
        newTotalPoints = leaderboard.getTotalPoints(competitor, raceColumn, getTimePoint());
        return newTotalPoints;
    }
}
