package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.operationaltransformation.Transformer;
import com.sap.sailing.server.RacingEventService;

public interface RacingEventServiceOperation<ResultType> extends Operation<RacingEventService>, Serializable {
    /**
     * Performs the actual operation, applying it to the <code>toState</code> service. The operation's result is
     * returned.
     */
    ResultType internalApplyTo(RacingEventService toState) throws Exception;
    
    /**
     * Implements the specific transformation rule for the implementing subclass for the set of possible peer operations
     * along which to transform this operation, assuming this is the client operation. See
     * {@link Transformer#transform(Operation, Operation)} for the specification.
     * 
     * @return the result of transforming <code>this</code> operation along <code>serverOp</code>
     */
    RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp);

    /**
     * Implements the specific transformation rule for the implementing subclass for the set of possible peer operations
     * along which to transform this operation, assuming this is the server operation. See
     * {@link Transformer#transform(Operation, Operation)} for the specification.
     * 
     * @return the result of transforming <code>this</code> operation along <code>clientOp</code>
     */
    RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp);

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

    RacingEventServiceOperation<?> transformAddLeaderboardClientOp(CreateLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformAddLeaderboardServerOp(CreateLeaderboard addLeaderboard);

    RacingEventServiceOperation<?> transformRemoveLeaderboardClientOp(RemoveLeaderboard removeLeaderboard);

    RacingEventServiceOperation<?> transformRemoveLeaderboardServerOp(RemoveLeaderboard removeLeaderboard);

    RacingEventServiceOperation<?> transformAddColumnToLeaderboardClientOp(AddColumnToLeaderboard addColumnToLeaderboard);

    RacingEventServiceOperation<?> transformAddColumnToLeaderboardServerOp(AddColumnToLeaderboard addColumnToLeaderboard);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownClientOp(MoveLeaderboardColumnDown moveLeaderboardColumnDown);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnDownServerOp(MoveLeaderboardColumnDown moveLeaderboardColumnDown);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpClientOp(MoveLeaderboardColumnUp moveLeaderboardColumnUp);

    RacingEventServiceOperation<?> transformMoveLeaderboardColumnUpServerOp(MoveLeaderboardColumnUp moveLeaderboardColumnUp);
}
