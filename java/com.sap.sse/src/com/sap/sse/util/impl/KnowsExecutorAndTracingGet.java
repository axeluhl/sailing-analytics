package com.sap.sse.util.impl;

import java.util.concurrent.Future;

import com.sap.sse.util.ThreadPoolUtil;

/**
 * Can be used with a {@link Future} task and outputs trace messages to the log after {@link #MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET} milliseconds
 * of a {@link #get()} call not returning. When used in conjunction with a {@link NamedTracingScheduledThreadPoolExecutor} such as
 * the ones returned by {@link ThreadPoolUtil}, the task will be 
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <V>
 */
public interface KnowsExecutorAndTracingGet<V> extends KnowsExecutor, HasTracingGet<V> {
    /**
     * Assuming that the thread pool's thread has taken over and is about to run this task,
     * sets all inheritable thread local values for the executing thread. The values
     * are obtained through {@link #getThreadLocalValuesToInherit()}.
     */
    void setInheritableThreadLocalValues();
    
    /**
     * Removes / "unsets" the values for all thread locals whose values were memorized by
     * {@link #getThreadLocalValuesToInherit()}. Call this method at the end of the
     * call/run method of this task.
     */
    void removeInheritableThreadLocalValues();
}
