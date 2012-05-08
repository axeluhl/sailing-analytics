package com.sap.sailing.server.operationaltransformation;

import java.util.Date;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboardScoreCorrection extends AbstractLeaderboardColumnOperation<Pair<Integer, Integer>> {
    private static final long serialVersionUID = -977025759476022993L;
    private final String competitorIdAsString;
    private final Integer correctedScore;
    private final Date date;
    
    public UpdateLeaderboardScoreCorrection(String leaderboardName, String columnName, String competitorIdAsString,
            Integer correctedScore, Date date) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
        this.correctedScore = correctedScore;
        this.date = date;
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
    public Pair<Integer, Integer> internalApplyTo(RacingEventService toState) throws NoWindException {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        int newNetPoints;
        int newTotalPoints;
        if (leaderboard != null) {
            MillisecondsTimePoint timePoint = new MillisecondsTimePoint(date);
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                if (correctedScore == null) {
                    leaderboard.getScoreCorrection().uncorrectScore(competitor, raceColumn);
                    newNetPoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                } else {
                    leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
                    newNetPoints = correctedScore;
                }
                newTotalPoints = leaderboard.getEntry(competitor, raceColumn, timePoint).getTotalPoints();
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        toState.updateStoredLeaderboard(leaderboard);
        return new Pair<Integer, Integer>(newNetPoints, newTotalPoints);
    }

}
