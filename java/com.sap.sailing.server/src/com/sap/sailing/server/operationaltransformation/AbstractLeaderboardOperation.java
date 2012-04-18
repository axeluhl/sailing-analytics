package com.sap.sailing.server.operationaltransformation;



public abstract class AbstractLeaderboardOperation extends AbstractRacingEventServiceOperation {
    private static final long serialVersionUID = 5377910723455422127L;
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

}
