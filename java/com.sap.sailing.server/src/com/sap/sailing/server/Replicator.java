package com.sap.sailing.server;

import com.sap.sse.replication.OperationExecutionListener;

public interface Replicator {
    /**
     * Replicates an operation to any replica of this replicator known and
     * {@link OperationExecutionListener#executed(RacingEventServiceOperation) notifies} all registered
     * operation execution listeners about the execution of the operation.
     */
    <T> void replicate(RacingEventServiceOperation<T> operation);
}
