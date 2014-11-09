package com.sap.sse.server.replication;

import com.sap.sse.operationaltransformation.Operation;

/**
 * An operational transformation {@link Operation} is expected to return the target state after applying the operation.
 * This operation type offers the possibility to let an operation return a result value from its
 * {@link #internalApplyTo} method while in the context of operational transformation the operation will still return
 * the target state.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface OperationWithResult<S, R> extends Operation<S> {
    /**
     * Performs the actual operation, applying it to the <code>toState</code> service. The operation's result is
     * returned.
     */
    R internalApplyTo(S toState) throws Exception;
}
