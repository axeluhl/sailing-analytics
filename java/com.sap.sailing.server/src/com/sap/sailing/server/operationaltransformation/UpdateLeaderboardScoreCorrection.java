package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.server.RacingEventService;

public class UpdateLeaderboardScoreCorrection extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = -977025759476022993L;
    private final String competitorIdAsString;
    private final Integer correctedScore;
    
    public UpdateLeaderboardScoreCorrection(String leaderboardName, String columnName, String competitorIdAsString,
            Integer correctedScore) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
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
    public Void internalApplyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                if (raceColumn == null) {
                    throw new IllegalArgumentException("Didn't find race "+getColumnName()+" in leaderboard "+getLeaderboardName());
                }
                if (correctedScore == null) {
                    leaderboard.getScoreCorrection().uncorrectScore(competitor, raceColumn);
                } else {
                    leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
                }
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        toState.updateStoredLeaderboard(leaderboard);
        return null;
    }

}
