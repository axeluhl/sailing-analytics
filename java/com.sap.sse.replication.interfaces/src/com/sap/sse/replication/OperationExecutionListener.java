package com.sap.sse.replication;


/**
 * Can be registered on a {@link Replicable} and will receive notifications about the execution of
 * operations.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the type of the state to which the operations are applied
 */
public interface OperationExecutionListener<S> {
    <T> void executed(OperationWithResult<S, T> operation);
}