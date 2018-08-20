package com.sap.sse.replication.impl;

import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;

/**
 * Listens for operation execution notifications from a single {@link Replicable} object. It asks the
 * {@link ReplicationServiceImpl} it was passed at construction to
 * {@link ReplicationServiceImpl#broadcastOperation(OperationWithResult, Replicable<S, O>) broadcast} the operation to any interested
 * replica.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S>
 */
public class ReplicationServiceExecutionListener<S> implements OperationExecutionListener<S> {
    private final ReplicationServiceImpl replicationService;
    private final Replicable<S, ?> replicable;
    
    /**
     * {@link Replicable#addOperationExecutionListener(OperationExecutionListener) Adds} this new listener to the <code>replicable</code>
     */
    protected ReplicationServiceExecutionListener(ReplicationServiceImpl replicationService, Replicable<S, ?> replicable) {
        super();
        this.replicationService = replicationService;
        this.replicable = replicable;
        this.replicable.addOperationExecutionListener(this);
    }

    /**
     * {@link #broadcastOperation(OperationWithResult, Replicable<S, O>) Broadcasts} the <code>operation</code> to all registered
     * replicas by publishing it to the fan-out exchange.
     */
    @Override
    public <T> void executed(OperationWithResult<S, T> operation) {
        try {
            replicationService.broadcastOperation(operation, replicable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unsubscribe() {
        this.replicable.removeOperationExecutionListener(this);
    }
}
