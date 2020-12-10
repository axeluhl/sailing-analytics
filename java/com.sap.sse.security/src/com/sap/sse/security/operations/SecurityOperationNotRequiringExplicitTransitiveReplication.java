package com.sap.sse.security.operations;

/**
 * To be implemented by those {@link SecurityOperation}s that do not require explicit transitive replication.
 * This interface has a default implementation for the {@link #isRequiresExplicitTransitiveReplication()} method
 * that always returns {@code false}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ResultType>
 */
public interface SecurityOperationNotRequiringExplicitTransitiveReplication<ResultType> extends SecurityOperation<ResultType> {
    /**
     * By default, the security-related operations will perform an update on the receiving replica that in turn
     * will trigger replication of this change transitively.
     */
    default boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
}
