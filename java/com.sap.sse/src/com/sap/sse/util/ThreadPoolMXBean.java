package com.sap.sse.util;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sse.common.Named;

public interface ThreadPoolMXBean extends Named {
    ObjectName getObjectName() throws MalformedObjectNameException;
    int getCorePoolSize();
    int getPoolSize();
    long getCompletedTaskCount();
    long getLargestPoolSize();
    int getMaximumPoolSize();
    int getActiveCount();
    long getTaskCount();
    int getQueuedTasks();
}
