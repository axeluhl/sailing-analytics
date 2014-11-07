package com.sap.sailing.server.replication;

import com.sap.sse.operationaltransformation.Operation;

public interface OperationExecutionListener {
    <T> void executed(Operation<T> operation);
}