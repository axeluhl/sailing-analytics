package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class DeleteAclOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final QualifiedObjectIdentifier idOfAccessControlledObject;

    public DeleteAclOperation(QualifiedObjectIdentifier idOfAccessControlledObject) {
        this.idOfAccessControlledObject = idOfAccessControlledObject;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalDeleteAcl(idOfAccessControlledObject);
        return null;
    }
}
