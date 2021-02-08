package com.sap.sse.util.impl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sse.util.ThreadPoolMXBean;

public class ThreadPoolMXBeanImpl implements ThreadPoolMXBean {
    private static final long serialVersionUID = -7057754487764427044L;
    private final NamedTracingScheduledThreadPoolExecutor threadPool;
    
    public ThreadPoolMXBeanImpl(NamedTracingScheduledThreadPoolExecutor threadPool) {
        super();
        this.threadPool = threadPool;
    }

    @Override
    public String getName() {
        return threadPool.getName();
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.sap.sse:type=ThreadPool,name="+ObjectName.quote(getName()));
    }

    @Override
    public int getCorePoolSize() {
        return threadPool.getCorePoolSize();
    }

    @Override
    public int getPoolSize() {
        return threadPool.getPoolSize();
    }

    @Override
    public long getCompletedTaskCount() {
        return threadPool.getCompletedTaskCount();
    }

    @Override
    public long getLargestPoolSize() {
        return threadPool.getCompletedTaskCount();
    }

    @Override
    public int getMaximumPoolSize() {
        return threadPool.getMaximumPoolSize();
    }

    @Override
    public int getActiveCount() {
        return threadPool.getMaximumPoolSize();
    }

    @Override
    public long getTaskCount() {
        return threadPool.getTaskCount();
    }

    @Override
    public int getQueuedTasks() {
        return threadPool.getQueue().size();
    }

}
