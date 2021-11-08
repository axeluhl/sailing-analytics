package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Adds one or more leaderboard groups to an event. Leaderboard group(s) and event are specified by their UUID.
 * The event including the leaderboard group links is updated to the database.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AddLeaderboardGroupToEvent extends AbstractRacingEventServiceOperation<Iterable<LeaderboardGroup>> {
    private static Logger logger = Logger.getLogger(AddLeaderboardGroupToEvent.class.getName());
    private static final long serialVersionUID = 5061411909275963501L;
    private final UUID eventId;
    private final UUID[] leaderboardGroupIds;

    public AddLeaderboardGroupToEvent(UUID eventId, UUID... leaderboardGroupIds) {
        super();
        this.eventId = eventId;
        this.leaderboardGroupIds = leaderboardGroupIds;
    }

    @Override
    public Iterable<LeaderboardGroup> internalApplyTo(RacingEventService toState) throws Exception {
        boolean updated = false;
        Event event = toState.getEvent(eventId);
        List<LeaderboardGroup> result = new ArrayList<>();
        if (event != null) {
            for (UUID leaderboardGroupId : leaderboardGroupIds) {
                final LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByID(leaderboardGroupId);
                if (leaderboardGroup != null) {
                    event.addLeaderboardGroup(leaderboardGroup);
                    result.add(leaderboardGroup);
                    updated = true;
                } else {
                    logger.info("Leaderboard group with ID " + leaderboardGroupId
                            + " not found. Cannot add leaderboard group to event with ID " + eventId);
                }
            }
        } else {
            logger.info("Event with ID " + eventId + " not found. Cannot add leaderboard groups with IDs "
                    + Arrays.toString(leaderboardGroupIds) + " to it.");
        }
        if (updated) {
            toState.getMongoObjectFactory().storeEvent(event); // updated==true implies event!=null
        }
        return result;
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
