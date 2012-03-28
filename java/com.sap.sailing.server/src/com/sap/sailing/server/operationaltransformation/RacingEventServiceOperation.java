package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.operationaltransformation.Transformer;
import com.sap.sailing.server.RacingEventService;

public interface RacingEventServiceOperation extends Operation<RacingEventService> {
    /**
     * Implements the specific transformation rule for the implementing subclass for the set of possible
     * peer operations along which to transform this operation. See {@link Transformer#transform(Operation, Operation)}
     * for the specification.
     */
    RacingEventServiceOperation transformFor(RacingEventServiceOperation peerOp);
}
