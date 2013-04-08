package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveLeaderboardColumn extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = 5425526859417359535L;

    public RemoveLeaderboardColumn(String columnName, String leaderboardName) {
        super(leaderboardName, columnName);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.removeLeaderboardColumn(getLeaderboardName(), getColumnName());
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformRemoveColumnFromLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformRemoveColumnFromLeaderboardServerOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnClientOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        if (affectsSameColumn(renameLeaderboardColumnClientOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return renameLeaderboardColumnClientOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnServerOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        if (affectsSameColumn(renameLeaderboardColumnServerOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return renameLeaderboardColumnServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardServerOp(
            RemoveLeaderboardColumn removeColumnFromLeaderboardServerOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardServerOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeColumnFromLeaderboardServerOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardClientOp(
            RemoveLeaderboardColumn removeColumnFromLeaderboardClientOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardClientOp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return removeColumnFromLeaderboardClientOp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownClientOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        if (affectsSameColumn(moveLeaderboardColumnDown)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnDown;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownServerOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        if (affectsSameColumn(moveLeaderboardColumnDown)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnDown;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpClientOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        if (affectsSameColumn(moveLeaderboardColumnUp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnUp;
        }
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpServerOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        if (affectsSameColumn(moveLeaderboardColumnUp)) {
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return moveLeaderboardColumnUp;
        }
    }

}
