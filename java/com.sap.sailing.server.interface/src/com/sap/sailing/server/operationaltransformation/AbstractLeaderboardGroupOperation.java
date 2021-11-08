package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

public abstract class AbstractLeaderboardGroupOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 4341066708061847418L;
    protected final UUID leaderboardGroupId;
    
    public AbstractLeaderboardGroupOperation(UUID leaderboardGroupId) {
        super();
        this.leaderboardGroupId = leaderboardGroupId;
    }

    protected UUID getLeaderboardGroupId() {
        return leaderboardGroupId;
    }

    protected boolean affectsSameLeaderboardGroup(AbstractLeaderboardGroupOperation<?> other) {
        return getLeaderboardGroupId().equals(other.getLeaderboardGroupId());
    }

}
