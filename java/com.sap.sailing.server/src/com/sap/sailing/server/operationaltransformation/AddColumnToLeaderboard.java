package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

/**
 * Doesn't transform for the {@link RemoveLeaderboardColumn} because only existing leaderboards can be removed,
 * and adding is only possible for non-existing names.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AddColumnToLeaderboard extends AbstractLeaderboardColumnOperation {
    private static final long serialVersionUID = -7670764349119941051L;
    private final boolean medalRace;
    
    public AddColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        super(leaderboardName, columnName);
        this.medalRace = medalRace;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.addColumnToLeaderboard(getColumnName(), getLeaderboardName(), medalRace);
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformAddColumnToLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformAddColumnToLeaderboardServerOp(this);
    }
}
