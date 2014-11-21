package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public interface SecurityOperation<ResultType> extends OperationWithResult<ReplicableSecurityService, ResultType> {
}
