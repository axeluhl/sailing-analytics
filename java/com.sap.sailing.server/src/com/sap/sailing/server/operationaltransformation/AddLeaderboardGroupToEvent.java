package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddLeaderboardGroupToEvent extends AbstractRacingEventServiceOperation<LeaderboardGroup> {
    private static final long serialVersionUID = 5061411909275963501L;
    private final UUID eventId;
    private final String leaderboardGroupName;

    public AddLeaderboardGroupToEvent(UUID eventId, String leaderboardGroupName) {
        super();
        this.eventId = eventId;
        this.leaderboardGroupName = leaderboardGroupName;
    }

    @Override
    public LeaderboardGroup internalApplyTo(RacingEventService toState) throws Exception {
        LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByName(leaderboardGroupName);
        toState.getEvent(eventId).addLeaderboardGroup(leaderboardGroup);
        return leaderboardGroup;
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
