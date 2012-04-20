package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Doesn't transform for the {@link RemoveLeaderboardColumn} because only existing leaderboards can be removed,
 * and adding is only possible for non-existing names.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AddColumnToLeaderboard extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = -7670764349119941051L;
    private final boolean medalRace;
    
    public AddColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        super(leaderboardName, columnName);
        this.medalRace = medalRace;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.addColumnToLeaderboard(getColumnName(), getLeaderboardName(), medalRace);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformAddColumnToLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformAddColumnToLeaderboardServerOp(this);
    }
}
