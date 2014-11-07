package com.sap.sailing.server;

import java.io.Serializable;

import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreatePersistentCompetitor;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnDown;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnUp;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboardColumn;
import com.sap.sse.operationaltransformation.Operation;

public interface RacingEventServiceOperation<ResultType> extends Operation<ResultType>, Serializable {
    /**
     * Performs the actual operation, applying it to the <code>toState</code> service. The operation's result is
     * returned.
     */
    ResultType internalApplyTo(RacingEventService toState) throws Exception;
    
    /**
     * Assumes this is the "server" operation and transforms the client's <code>removeColumnFromLeaderboardClientOp</code> according to this
     * operation. The default implementation will probably pass on the untransformed client operation. However, if this
     * operation deals with the leaderboard column being removed by <code>removeColumnFromLeaderboardClientOp</code>,
     * the result will be <code>null</code>, meaning that this operation cannot be applied after the column has been removed.
     */
    RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardClientOp(RemoveLeaderboardColumn removeColumnFromLeaderboardClientOp);

    RacingEventServiceOperation<?> transformRemoveColumnFromLeaderboardServerOp(RemoveLeaderboardColumn removeColumnFromLeaderboardServerOp);

    RacingEventServiceOperation<?> transformRenameLeaderboardColumnClientOp(RenameLeaderboardColumn renameLeaderboardColumnClientOp);

    RacingEventServiceOperation<?> transformRenameLeaderboardColumnServerOp(RenameLeaderboardColumn renameLeaderboardColumnServerOp);

    RacingEventServiceOperation<?> transformAddFlexibleLeaderboardClientOp(CreateFlexibleLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformAddFlexibleLeaderboardServerOp(CreateFlexibleLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformAddRegattaLeaderboardClientOp(CreateRegattaLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformAddRegattaLeaderboardServerOp(CreateRegattaLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard);

    RacingEventServiceOperation<?> transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard);

    RacingEventServiceOperation<?> transformAddColumnToLeaderboardClientOp(AddColumnToLeaderboard addColumnToLeaderboard);

    RacingEventServiceOperation<?> transformAddColumnToLeaderboardServerOp(AddColumnToLeaderboard addColumnToLeaderboard);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownClientOp(MoveLeaderboardColumnDown moveLeaderboardColumnDown);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownServerOp(MoveLeaderboardColumnDown moveLeaderboardColumnDown);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpClientOp(MoveLeaderboardColumnUp moveLeaderboardColumnUp);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpServerOp(MoveLeaderboardColumnUp moveLeaderboardColumnUp);

    RacingEventServiceOperation<?> transformCreatePersistentCompetitorClientOp(CreatePersistentCompetitor createPersistentCompetitor);

    RacingEventServiceOperation<?> transformCreatePersistentCompetitorServerOp(CreatePersistentCompetitor createPersistentCompetitor);
}
