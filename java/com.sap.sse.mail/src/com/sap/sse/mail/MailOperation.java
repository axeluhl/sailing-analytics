package com.sap.sse.mail;

import com.sap.sse.mail.impl.ReplicableMailService;
import com.sap.sse.replication.OperationWithResult;

public interface MailOperation<ResultT> extends OperationWithResult<ReplicableMailService, ResultT> {
    /**
     * By default, the mail-related operations will perform an update on the receiving replica that in turn
     * will trigger replication of this change transitively.
     */
    default boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }

}
