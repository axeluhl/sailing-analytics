package com.sap.sailing.server.operationaltransformation;

public abstract class AbstractLeaderboardGroupOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 4341066708061847418L;
    private final String leaderboardGroupName;
    
    public AbstractLeaderboardGroupOperation(String leaderboardGroupName) {
        super();
        this.leaderboardGroupName = leaderboardGroupName;
    }

    protected String getLeaderboardGroupName() {
        return leaderboardGroupName;
    }

    protected boolean affectsSameLeaderboardGroup(AbstractLeaderboardGroupOperation<?> other) {
        return getLeaderboardGroupName().equals(other.getLeaderboardGroupName());
    }

}
