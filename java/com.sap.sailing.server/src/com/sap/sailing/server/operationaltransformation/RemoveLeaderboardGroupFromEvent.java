package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveLeaderboardGroupFromEvent extends AbstractRacingEventServiceOperation<Boolean> {
    private static final long serialVersionUID = 5061411909275963501L;
    private final UUID eventId;
    private final String leaderboardGroupName;

    public RemoveLeaderboardGroupFromEvent(UUID eventId, String leaderboardGroupName) {
        super();
        this.eventId = eventId;
        this.leaderboardGroupName = leaderboardGroupName;
    }

    @Override
    public Boolean internalApplyTo(RacingEventService toState) throws Exception {
        LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByName(leaderboardGroupName);
        return toState.getEvent(eventId).removeLeaderboardGroup(leaderboardGroup);
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
