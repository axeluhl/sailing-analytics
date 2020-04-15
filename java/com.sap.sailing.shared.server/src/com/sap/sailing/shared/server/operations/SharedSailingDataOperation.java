package com.sap.sailing.shared.server.operations;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;
import com.sap.sse.replication.OperationWithResult;

public interface SharedSailingDataOperation<ResultType>
        extends OperationWithResult<ReplicatingSharedSailingData, ResultType> {

}
