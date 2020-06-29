package com.sap.sse.landscape.mongodb;

import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface MongoProcess extends Process<RotatingFileBasedLog, MongoMetrics> {
    boolean isHidden();
}
