package com.sap.sse.server.replication.impl;

import com.sap.sse.server.replication.OperationWithResult;
import com.sap.sse.server.replication.Replicable;

public interface HasReplicable<S, O extends OperationWithResult<S, ?>> {
    Replicable<S, O> getReplicable();
}
