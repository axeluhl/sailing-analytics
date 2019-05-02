package com.sap.sse.replication;

import java.io.Serializable;

import com.sap.sse.operationaltransformation.Operation;

/**
 * An operational transformation {@link Operation} is expected to return the target state after applying the operation.
 * This operation type offers the possibility to let an operation return a result value from its
 * {@link #internalApplyTo} method while in the context of operational transformation the operation will still return
 * the target state.
 * 
 * @param <S>
 *            type of state to which the operation can be applied
 * @param <R>
 *            result type, or in other words, the return type of the {@link #internalApplyTo(Object)} operation
 * 
 * @author Axel Uhl (D043530)
 *
 */
@FunctionalInterface
public interface OperationWithResult<S, R> extends Operation<S>, Serializable {
    /**
     * Performs the actual operation, applying it to the <code>toState</code> service. The operation's result is
     * returned.<p>
     * 
     * <b>NOTE:</b> If invoking this method triggers replication of the operation's effects, this object must
     * return <code>false</code> from the {@link #isRequiresExplicitTransitiveReplication()} method to avoid
     * replication duplication in transitive replication scenarios.
     */
    R internalApplyTo(S toState) throws Exception;
    
    /**
     * Tells whether this operation requires explicit transitive replication to other replicas when received by a
     * replica. This is the case for all operations whose {@link #internalApplyTo(Object)} method will not trigger
     * replication. An example for an operation that does <em>not</em> require explicit transitive replication is the
     * insertion of a GPS fix into a competitor's track because the insertion of the fix, as implemented by
     * {@link #internalApplyTo} will trigger the replication.
     * <p>
     * 
     * This default implementation returns <code>true</code>. Implementing classes whose
     * {@link #internalApplyTo(Object)} operation implicitly triggers replication must override this to return
     * <code>false</code>.
     */
    default boolean isRequiresExplicitTransitiveReplication() {
        return true;
    }

    /**
     * Ignores the actual result of {@link #internalApplyTo(Object)} and returns <code>toState</code> which
     * for the operational transformation algorithm is the "next state reached."
     */
    @Override
    default S applyTo(S toState) {
        try {
            internalApplyTo(toState);
            return toState;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
