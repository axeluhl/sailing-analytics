package com.sap.sse.mail.operations;

import com.sap.sse.mail.impl.ReplicableMailService;
import com.sap.sse.replication.OperationWithResult;

public interface MailServiceOperation<ResultType> extends OperationWithResult<ReplicableMailService, ResultType> {

}
