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
}
