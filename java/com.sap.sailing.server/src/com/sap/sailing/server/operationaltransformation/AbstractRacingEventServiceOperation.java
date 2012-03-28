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
    public RacingEventServiceOperation transformClientRenameLeaderboardColumnOp(
            RenameLeaderboardColumn renameLeaderboardColumnClientOp) {
        return renameLeaderboardColumnClientOp;
    }

    @Override
    public RacingEventServiceOperation transformServerRenameLeaderboardColumnOp(
            RenameLeaderboardColumn renameLeaderboardColumnServerOp) {
        return renameLeaderboardColumnServerOp;
    }

    @Override
    public RacingEventServiceOperation transformServerRemoveColumnFromLeaderboard(RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        return removeColumnFromLeaderboardServerOp;
    }

    @Override
    public RacingEventServiceOperation transformClientRemoveColumnFromLeaderboard(RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        return removeColumnFromLeaderboardClientOp;
    }
    
    public static RacingEventServiceOperation getNoOp() {
        return new AbstractRacingEventServiceOperation() {
            @Override
            public RacingEventService applyTo(RacingEventService toState) {
                return toState;
            }

            @Override
            public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
                return serverOp;
            }

            @Override
            public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
                return clientOp;
            }
        };
    }
}
