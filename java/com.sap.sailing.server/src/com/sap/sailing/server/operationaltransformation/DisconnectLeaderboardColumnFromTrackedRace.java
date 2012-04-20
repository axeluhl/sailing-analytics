package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.server.RacingEventService;

public class DisconnectLeaderboardColumnFromTrackedRace extends AbstractLeaderboardColumnOperation<Void> {

    private static final long serialVersionUID = -5822713961135743309L;

    public DisconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String columnName) {
        super(leaderboardName, columnName);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(getColumnName());
            if (raceColumn != null) {
                raceColumn.setTrackedRace(null);
                raceColumn.setRaceIdentifier(null);
                toState.updateStoredLeaderboard(leaderboard);
            } else {
                throw new IllegalArgumentException("Didn't find race "+getColumnName()+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        return null;
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

}
