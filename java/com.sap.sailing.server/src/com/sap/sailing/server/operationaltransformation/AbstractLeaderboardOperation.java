package com.sap.sailing.server.operationaltransformation;



public abstract class AbstractLeaderboardOperation extends AbstractRacingEventServiceOperation {
    private final String leaderboardName;
    
    public AbstractLeaderboardOperation(String leaderboardName) {
        super();
        this.leaderboardName = leaderboardName;
    }

    protected String getLeaderboardName() {
        return leaderboardName;
    }

    protected boolean affectsSameLeaderboard(AbstractLeaderboardOperation other) {
        return getLeaderboardName().equals(other.getLeaderboardName());
    }

    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }


    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }
    
}
