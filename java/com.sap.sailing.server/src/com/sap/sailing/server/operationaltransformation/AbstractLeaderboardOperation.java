package com.sap.sailing.server.operationaltransformation;



public abstract class AbstractLeaderboardOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 5377910723455422127L;
    private final String leaderboardName;
    
    public AbstractLeaderboardOperation(String leaderboardName) {
        super();
        this.leaderboardName = leaderboardName;
    }

    protected String getLeaderboardName() {
        return leaderboardName;
    }

    protected boolean affectsSameLeaderboard(AbstractLeaderboardOperation<?> other) {
        return getLeaderboardName().equals(other.getLeaderboardName());
    }

}
