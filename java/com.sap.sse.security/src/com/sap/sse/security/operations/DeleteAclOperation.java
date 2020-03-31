package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class DeleteAclOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -8775552149162981191L;
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
