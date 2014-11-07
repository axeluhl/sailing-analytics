package com.sap.sailing.server.replication;

import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.operationaltransformation.OperationWithTransformationSupport;

public interface OperationExecutionListener {
    <T> void executed(OperationWithTransformationSupport<?, ? extends Operation<T>> operation);
}