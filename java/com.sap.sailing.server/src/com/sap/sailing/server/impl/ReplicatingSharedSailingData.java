package com.sap.sailing.server.impl;

import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

public interface ReplicatingSharedSailingData extends SharedSailingData,
        ReplicableWithObjectInputStream<ReplicatingSharedSailingData, OperationWithResult<ReplicatingSharedSailingData, ?>> {

}
