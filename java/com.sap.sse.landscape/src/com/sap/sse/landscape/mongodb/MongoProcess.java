package com.sap.sse.landscape.mongodb;

import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface MongoProcess extends Process<RotatingFileBasedLog, MongoMetrics> {
    int DEFAULT_PORT = 27017;
    boolean isHidden();
    int getPriority();
    int getVotes();
}
