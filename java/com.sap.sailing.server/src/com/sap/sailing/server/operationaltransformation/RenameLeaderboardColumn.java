package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.ClientServerOperationPair;
import com.sap.sailing.server.RacingEventService;

public class RenameLeaderboardColumn extends AbstractLeaderboardColumnOperation {
    private final String newColumnName;
    
    public RenameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        super(leaderboardName, oldColumnName);
        this.newColumnName = newColumnName;
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.renameLeaderboardColumn(getLeaderboardName(), getColumnName(), newColumnName);
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformClientRenameLeaderboardColumnOp(this);
    }


    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformServerRenameLeaderboardColumnOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerRemoveColumnFromLeaderboard(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardServerOp)) {
            return ClientServerOperationPair.getNoOp(); // don't need the rename anymore when column is removed
        } else {
            return removeColumnFromLeaderboardServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation transformClientRemoveColumnFromLeaderboard(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardClientOp)) {
            return ClientServerOperationPair.getNoOp(); // don't need the rename anymore when column is removed
        } else {
            return removeColumnFromLeaderboardClientOp;
        }
    }

}
