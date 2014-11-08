package com.sap.sailing.server.replication.impl;

import com.sap.sailing.server.replication.OperationWithResult;
import com.sap.sailing.server.replication.Replicable;

public interface HasReplicable<S, O extends OperationWithResult<S, ?>> {
    Replicable<S, O> getReplicable();
}
