package com.sap.sse.replication.impl;

import java.io.IOException;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;

public interface OperationSerializerBuffer {
    <S, O extends OperationWithResult<S, ?>> void write(final OperationWithResult<?, ?> operation, Replicable<S, O> replicable) throws IOException;
}
