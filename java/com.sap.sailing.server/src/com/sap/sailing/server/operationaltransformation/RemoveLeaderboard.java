package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class RemoveLeaderboard extends AbstractLeaderboardOperation {
    public RemoveLeaderboard(String leaderboardName) {
        super(leaderboardName);
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformRemoveLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformRemoveLeaderboardServerOp(this);
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.removeLeaderboard(getLeaderboardName());
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformRenameLeaderboardColumnClientOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformRenameLeaderboardColumnServerOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardServerOp(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardClientOp(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformAddColumnToLeaderboardClientOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformAddColumnToLeaderboardServerOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return AbstractRacingEventServiceOperation.getNoOp();
    }

    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }


    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }
    
}
