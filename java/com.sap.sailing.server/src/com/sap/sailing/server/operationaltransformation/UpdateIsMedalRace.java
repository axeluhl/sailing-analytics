package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateIsMedalRace extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = 5925081961634860757L;
    private final boolean isMedalRace;

    public UpdateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) {
        super(leaderboardName, columnName);
        this.isMedalRace = isMedalRace;
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
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard named " + getLeaderboardName() + " not found");
        } else  if (!(leaderboard instanceof FlexibleLeaderboard)) {
            throw new IllegalArgumentException("Medal race settings cannot be changed in leaderboard named " + getLeaderboardName());
        } else {
            ((FlexibleLeaderboard) leaderboard).updateIsMedalRace(getColumnName(), isMedalRace);
            updateStoredLeaderboard(toState, leaderboard);
        }
        return null;
    }

}
