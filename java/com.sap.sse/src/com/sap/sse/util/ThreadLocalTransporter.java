package com.sap.sse.util;

/**
 * When a task is to be pushed out to another thread, {@link ThreadLocal} state may need
 * to be preserved and re-established so that the task, when executed by another thread
 * will see the {@link ThreadLocal}s' state just as it was when the task was created.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ThreadLocalTransporter {
    /**
     * Call this method when your task is created and to be submitted to an executor.
     * This will store the state of all relevant {@link ThreadLocal}s in this object.
     */
    void rememberThreadLocalStates();
    
    /**
     * Call this at the beginning of the task that is executed on another thread. This method
     * will save the states of the relevant {@link ThreadLocal}s and will then establish their
     * state as remembered earlier by the {@link #rememberThreadLocalStates()} method.
     */
    void pushThreadLocalStates();
    
    /**
     * Call this in a {@code finally} block at the end of your task. This will restore the
     * relevant {@link ThreadLocal} states as they were remembered when {@link #pushThreadLocalStates()}
     * was invoked.
     */
    void popThreadLocalStates();
}
