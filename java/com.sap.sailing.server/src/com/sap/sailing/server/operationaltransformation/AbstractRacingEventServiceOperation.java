package com.sap.sailing.server.operationaltransformation;


/**
 * Performs identical transformations for all operation types by simply returning the operation passed. Subclasses need
 * to override the transformation methods for those operation types that have an impact for them.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractRacingEventServiceOperation implements RacingEventServiceOperation {
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
}
