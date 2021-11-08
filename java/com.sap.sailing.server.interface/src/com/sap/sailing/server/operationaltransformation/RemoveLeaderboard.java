package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RemoveLeaderboard extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = -6491003416450255268L;

    public RemoveLeaderboard(String leaderboardName) {
        super(leaderboardName);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformRemoveLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformRemoveLeaderboardServerOp(this);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.removeLeaderboard(getLeaderboardName());
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnClientOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        if (affectsSameLeaderboard(renameLeaderboardColumnClientOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return renameLeaderboardColumnClientOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnServerOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        if (affectsSameLeaderboard(renameLeaderboardColumnServerOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return renameLeaderboardColumnServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardServerOp(
            RemoveLeaderboardColumn removeColumnFromLeaderboardServerOp) {
        if (affectsSameLeaderboard(removeColumnFromLeaderboardServerOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeColumnFromLeaderboardServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardClientOp(
            RemoveLeaderboardColumn removeColumnFromLeaderboardClientOp) {
        if (affectsSameLeaderboard(removeColumnFromLeaderboardClientOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeColumnFromLeaderboardClientOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformAddColumnToLeaderboardClientOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        if (affectsSameLeaderboard(addColumnToLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return addColumnToLeaderboard;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformAddColumnToLeaderboardServerOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        if (affectsSameLeaderboard(addColumnToLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return addColumnToLeaderboard;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeLeaderboard;
        }
    }


    @Override
    public RacingEventServiceOperation<?> transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard) {
        if (affectsSameLeaderboard(removeLeaderboard)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeLeaderboard;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownClientOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        if (affectsSameLeaderboard(moveLeaderboardColumnDown)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnDown;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownServerOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        if (affectsSameLeaderboard(moveLeaderboardColumnDown)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnDown;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpClientOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        if (affectsSameLeaderboard(moveLeaderboardColumnUp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnUp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpServerOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        if (affectsSameLeaderboard(moveLeaderboardColumnUp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnUp;
        }
    }
    
}
