package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public interface SecurityOperation<ResultType> extends OperationWithResult<ReplicableSecurityService, ResultType> {
    /**
     * By default, the security-related operations will perform an update on the receiving replica that in turn
     * will trigger replication of this change transitively.
     */
    default boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
}
