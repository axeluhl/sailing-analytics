package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class DeleteOwnershipOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final QualifiedObjectIdentifier idOfOwnedObject;

    public DeleteOwnershipOperation(QualifiedObjectIdentifier idOfOwnedObject) {
        this.idOfOwnedObject = idOfOwnedObject;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalDeleteOwnership(idOfOwnedObject);
        return null;
    }
}
