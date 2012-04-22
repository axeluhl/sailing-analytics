package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.server.RacingEventService;

public class UpdateLeaderboardMaxPointsReason extends AbstractLeaderboardColumnOperation {
    private static final long serialVersionUID = -492130952256848047L;
    private final String competitorIdAsString;
    private final MaxPointsReason newMaxPointsReason;
    
    public UpdateLeaderboardMaxPointsReason(String leaderboardName, String columnName, String competitorIdAsString,
            MaxPointsReason newMaxPointsReason) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
        this.newMaxPointsReason = newMaxPointsReason;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                if (raceColumn == null) {
                    throw new IllegalArgumentException("Didn't find race "+getColumnName()+" in leaderboard "+getLeaderboardName());
                }
                leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, newMaxPointsReason);
                toState.updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        return toState;
    }

}
