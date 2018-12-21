package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.interfaces.RacingEventServiceOperation;



/**
 * Performs identical transformations for all operation types by simply returning the operation passed. Subclasses need
 * to override the transformation methods for those operation types that have an impact for them.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractRacingEventServiceOperation<ResultType> implements RacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 3888231857034991271L;

    @Override
    public RacingEventServiceOperation<?> transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard) {
        return removeLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard) {
        return removeLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddFlexibleLeaderboardClientOp(CreateFlexibleLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddFlexibleLeaderboardServerOp(CreateFlexibleLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddRegattaLeaderboardClientOp(CreateRegattaLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddRegattaLeaderboardServerOp(CreateRegattaLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddRegattaLeaderboardClientOp(CreateRegattaLeaderboardWithEliminations addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddRegattaLeaderboardServerOp(CreateRegattaLeaderboardWithEliminations addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnClientOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        return renameLeaderboardColumnClientOp;
    }

    @Override
    public RacingEventServiceOperation<?> transformRenameLeaderboardColumnServerOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        return renameLeaderboardColumnServerOp;
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardServerOp(RemoveLeaderboardColumn removeColumnFromLeaderboardServerOp) {
        return removeColumnFromLeaderboardServerOp;
    }

    @Override
    public RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardClientOp(RemoveLeaderboardColumn removeColumnFromLeaderboardClientOp) {
        return removeColumnFromLeaderboardClientOp;
    }
    
    @Override
    public RacingEventServiceOperation<?> transformAddColumnToLeaderboardClientOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return addColumnToLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformAddColumnToLeaderboardServerOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return addColumnToLeaderboard;
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownClientOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        return moveLeaderboardColumnDown;
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownServerOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        return moveLeaderboardColumnDown;
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpClientOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        return moveLeaderboardColumnUp;
    }

    @Override
    public RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpServerOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        return moveLeaderboardColumnUp;
    }

    public static RacingEventServiceOperation<Void> getNoOp() {
        return null;
    }

    @Override
    public String toString() {
        return super.toString()+" [requiresSynchronousExecution="+requiresSynchronousExecution()+"]";
    }
    
}
