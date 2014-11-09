package com.sap.sse.replication.impl;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;

public interface HasReplicable<S, O extends OperationWithResult<S, ?>> {
    Replicable<S, O> getReplicable();
}
