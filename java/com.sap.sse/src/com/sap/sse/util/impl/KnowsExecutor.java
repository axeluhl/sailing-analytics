package com.sap.sse.util.impl;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A {@link ThreadPoolExecutor} can be set on this object. This is used, in particular, by the
 * {@link NamedTracingScheduledThreadPoolExecutor} whenever a task of this type is enqueued.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface KnowsExecutor {
    void setExecutorThisTaskIsScheduledFor(ThreadPoolExecutor executorThisTaskIsScheduledFor);
    Map<InheritableThreadLocal<Object>, Object> getThreadLocalValuesToInherit();
}
