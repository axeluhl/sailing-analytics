package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class AclAddPermissionOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final QualifiedObjectIdentifier idOfAccessControlledObject;
    protected final UUID groupId;
    protected final String action;

    public AclAddPermissionOperation(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId,
            String action) {
        this.idOfAccessControlledObject = idOfAccessControlledObject;
        this.groupId = groupId;
        this.action = action;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAclAddPermission(idOfAccessControlledObject, groupId, action);
        return null;
    }
}
