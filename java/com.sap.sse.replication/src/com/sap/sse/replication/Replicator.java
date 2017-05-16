package com.sap.sse.replication;

public interface Replicator<S, O extends OperationWithResult<S, ?>> {
    <T> void replicate(O operation);
}
