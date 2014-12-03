package com.sap.sse.util.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory by which the priority of the threads created can be set to something different from normal.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ThreadFactoryWithPriority implements ThreadFactory {
    private ThreadFactory defaultFactory;
    private final int priority;
    
    public ThreadFactoryWithPriority(int priority) {
        defaultFactory = Executors.defaultThreadFactory();
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread result = defaultFactory.newThread(r);
        result.setPriority(priority);
        return result;
    }
    
    
}
