package com.sap.sse.landscape.rabbitmq;

import com.sap.sse.common.Named;

public interface Exchange extends Named {
    Iterable<Queue> getReadingQueues();
    
    Queue getWritingQueue();
}
