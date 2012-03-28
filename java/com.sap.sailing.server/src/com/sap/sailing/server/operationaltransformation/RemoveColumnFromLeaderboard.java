package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.ClientServerOperationPair;
import com.sap.sailing.server.RacingEventService;

public class RemoveColumnFromLeaderboard extends AbstractLeaderboardColumnOperation {
    
    
    public RemoveColumnFromLeaderboard(String columnName, String leaderboardName) {
        super(leaderboardName, columnName);
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.removeLeaderboardColumn(getLeaderboardName(), getColumnName());
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformClientRemoveColumnFromLeaderboard(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformServerRemoveColumnFromLeaderboard(this);
    }

    @Override
    public RacingEventServiceOperation transformServerRemoveColumnFromLeaderboard(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardServerOp)) {
            // skip server's remove and hence only apply the client's remove operation
            return ClientServerOperationPair.getNoOp();
        } else {
            return removeColumnFromLeaderboardServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation transformClientRemoveColumnFromLeaderboard(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardClientOp)) {
            // skip client's remove and hence only apply the server's remove operation
            return ClientServerOperationPair.getNoOp();
        } else {
            return removeColumnFromLeaderboardClientOp;
        }
    }

}
