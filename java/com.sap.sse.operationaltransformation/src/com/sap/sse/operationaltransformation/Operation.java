package com.sap.sse.operationaltransformation;

/**
 * A change operation can be applied to some start state of some
 * universe and transforms that universe into a target state.
 * 
 * @author Axel Uhl D043530
 * 
 * @param <S> the class that models the state, on instances of which operations perform changes
 */
public interface Operation<S> extends Cloneable {
    /**
     * Applies this operation to the state <tt>toState</tt>, producing a new target state
     * which is returned.
     * 
     * @param toState the state to which to apply this operation
     * @return the state produced by applying this operation to <tt>toState</tt>
     */
    S applyTo(S toState);
    
    /**
     * Tells if this operation needs to be executed in order with other operations requesting synchronous execution.
     * This default implementation returns <code>true</code> which is safe but may be slower and using less concurrency
     * than possible. Implementations may make the assertion that parallel execution will do no harm by overriding this
     * method, returning <code>true</code>.
     */
    default boolean requiresSynchronousExecution() {
        return true;
    }

    /**
     * If it is allowed to apply this operation {@link #requiresSynchronousExecution() asynchronously} then this method
     * should provide a key such that two operations that may potentially block each other return equal keys.
     * <p>
     * 
     * For example, if this operation, when applied, will insert a GPS fix into a competitor's track, its key should be
     * the competitor ID combined with the race identifier because operations inserting fixes into the same track will
     * have to wait for each other.
     * <p>
     * 
     * Should operation implementations that {@link #requiresSynchronousExecution() allow for asynchronous execution}
     * leave this at the default implementation (which simply returns the operation's {@link #getClass() class}) or
     * return an otherwise inappropriate key that may differ across operations even though those operations would block
     * each other then this can cause performance degradations on the replicas where blocked operations clog the thread
     * pool used for running these asynchronous operations.
     * <p>
     * 
     * The result of this operation is used when scheduling the operation for execution. Those with equal keys will
     * be scheduled to execute sequentially within a single thread at any time, leaving the thread pools remaining threads
     * available for running other operations that won't block based on these operations.<p>
     * 
     * See also bug 5518.
     * 
     * @return This default implementation returns the {@link #getClass() implementation class} of this operation. This
     *         may result in less "spread" across the thread pool available on the replicas' side than may be possible
     *         by a finer-grained key returned by specializing classes. Implementations should return more specific
     *         objects that should be {@link Object#equals(Object) equal} if their respective operations may block each
     *         other. Of course, a consistent {@link Object#hashCode()} implementation must exist for any specific
     *         {@link Object#equals(Object)} implementation. For example, an operation for inserting GPS fixes into a
     *         competitor's track within a race should return a key consisting of components identifying the competitor
     *         and the race.
     */
    default Object getKeyForAsynchronousExecution() {
        return getClass();
    }
}
