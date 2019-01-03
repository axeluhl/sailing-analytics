package com.sap.sailing.server.operationaltransformation;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RemoveLeaderboardGroupFromEvent extends AbstractRacingEventServiceOperation<Boolean> {
    private static final long serialVersionUID = 5061411909275963501L;
    private static Logger logger = Logger.getLogger(RemoveLeaderboardGroupFromEvent.class.getName());
    private final UUID eventId;
    private final UUID[] leaderboardGroupIds;

    public RemoveLeaderboardGroupFromEvent(UUID eventId, UUID... leaderboardGroupIds) {
        super();
        this.eventId = eventId;
        this.leaderboardGroupIds = leaderboardGroupIds;
    }

    @Override
    public Boolean internalApplyTo(RacingEventService toState) {
        boolean updated = false;
        Event event = toState.getEvent(eventId);
        if (event != null) {
            for (UUID leaderboardGroupId : leaderboardGroupIds) {
                final LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByID(leaderboardGroupId);
                if (leaderboardGroup != null) {
                    event.removeLeaderboardGroup(leaderboardGroup);
                    updated = true;
                } else {
                    logger.info("Leaderboard group with ID " + leaderboardGroupId
                            + " not found. Cannot remove leaderboard group from event with ID " + eventId);
                }
            }
        } else {
            logger.info("Event with ID " + eventId + " not found. Cannot remove leaderboard groups with IDs "
                    + Arrays.toString(leaderboardGroupIds) + " from it.");
        }
        if (updated) {
            toState.getMongoObjectFactory().storeEvent(event); // updated==true implies event!=null
        }
        return updated;
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
