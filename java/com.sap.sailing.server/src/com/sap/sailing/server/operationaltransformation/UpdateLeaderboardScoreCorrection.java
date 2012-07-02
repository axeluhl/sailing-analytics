package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboardScoreCorrection extends AbstractLeaderboardColumnOperation<Triple<Integer, Integer, Boolean>> {
    private static final long serialVersionUID = -977025759476022993L;
    private final String competitorIdAsString;
    private final Integer correctedScore;
    private final TimePoint timePoint;
    
    /**
     * @param timePoint the time point for which to deliver leaderboard results as the result of this operation
     */
    public UpdateLeaderboardScoreCorrection(String leaderboardName, String columnName, String competitorIdAsString,
            Integer correctedScore, TimePoint timePoint) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
        this.correctedScore = correctedScore;
        this.timePoint = timePoint;
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
    public Triple<Integer, Integer, Boolean> internalApplyTo(RacingEventService toState) throws NoWindException {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        int newNetPoints;
        int newTotalPoints;
        boolean isScoreCorrected;
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceColumn raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                if (correctedScore == null) {
                    leaderboard.getScoreCorrection().uncorrectScore(competitor, raceColumn);
                    newNetPoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                } else {
                    leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
                    newNetPoints = correctedScore;
                }
                newTotalPoints = leaderboard.getEntry(competitor, raceColumn, timePoint).getTotalPoints();
                isScoreCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn);
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        updateStoredLeaderboard(toState, leaderboard);
        return new Triple<Integer, Integer, Boolean>(newNetPoints, newTotalPoints, isScoreCorrected);
    }

}
