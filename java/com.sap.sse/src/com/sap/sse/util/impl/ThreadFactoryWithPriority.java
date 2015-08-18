package com.sap.sse.util.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory by which the priority of the threads created can be set to something different from normal.
 * Clients can also tell whether the threads shall be created as daemon threads or user threads.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ThreadFactoryWithPriority implements ThreadFactory {
    private ThreadFactory defaultFactory;
    private final int priority;
    private final Boolean daemon;
    
    /**
     * @param daemon
     *            if <code>null</code>, the threads produced by this factory will be daemon threads if and only if the
     *            caller of {@link #newThread} runs in a daemon thread; otherwise, this parameter determines whether the
     *            threads created by this factory shall be daemon threads.
     */
    public ThreadFactoryWithPriority(int priority, Boolean daemon) {
        defaultFactory = Executors.defaultThreadFactory();
        this.priority = priority;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread result = defaultFactory.newThread(r);
        result.setPriority(priority);
        if (daemon != null) {
            result.setDaemon(daemon);
        }
        return result;
    }
    
    
}
