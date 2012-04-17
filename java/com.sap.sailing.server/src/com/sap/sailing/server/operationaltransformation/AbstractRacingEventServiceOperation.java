package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;



/**
 * Performs identical transformations for all operation types by simply returning the operation passed. Subclasses need
 * to override the transformation methods for those operation types that have an impact for them.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractRacingEventServiceOperation implements RacingEventServiceOperation {
    private static final long serialVersionUID = 3888231857034991271L;

    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard) {
        return removeLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard) {
        return removeLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformAddLeaderboardClientOp(AddLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformAddLeaderboardServerOp(AddLeaderboard addLeaderboard) {
        return addLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformRenameLeaderboardColumnClientOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        return renameLeaderboardColumnClientOp;
    }

    @Override
    public RacingEventServiceOperation transformRenameLeaderboardColumnServerOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        return renameLeaderboardColumnServerOp;
    }

    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardServerOp(RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        return removeColumnFromLeaderboardServerOp;
    }

    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardClientOp(RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        return removeColumnFromLeaderboardClientOp;
    }
    
    @Override
    public RacingEventServiceOperation transformAddColumnToLeaderboardClientOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return addColumnToLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformAddColumnToLeaderboardServerOp(
            AddColumnToLeaderboard addColumnToLeaderboard) {
        return addColumnToLeaderboard;
    }

    @Override
    public RacingEventServiceOperation transformMoveLeaderboardColumnDownClientOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        return moveLeaderboardColumnDown;
    }

    @Override
    public RacingEventServiceOperation transformMoveLeaderboardColumnDownServerOp(
            MoveLeaderboardColumnDown moveLeaderboardColumnDown) {
        return moveLeaderboardColumnDown;
    }

    @Override
    public RacingEventServiceOperation transformMoveLeaderboardColumnUpClientOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        return moveLeaderboardColumnUp;
    }

    @Override
    public RacingEventServiceOperation transformMoveLeaderboardColumnUpServerOp(
            MoveLeaderboardColumnUp moveLeaderboardColumnUp) {
        return moveLeaderboardColumnUp;
    }

    public static RacingEventServiceOperation getNoOp() {
        return new AbstractRacingEventServiceOperation() {
            private static final long serialVersionUID = -7203280393485688834L;

            @Override
            public RacingEventService applyTo(RacingEventService toState) {
                return toState;
            }

            @Override
            public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
                return this;
            }

            @Override
            public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
                return this;
            }
            
            @Override
            public String toString() {
                return "noop";
            }
        };
    }
}
