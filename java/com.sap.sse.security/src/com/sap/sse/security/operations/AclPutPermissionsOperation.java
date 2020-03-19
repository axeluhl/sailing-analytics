package com.sap.sse.security.operations;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class AclPutPermissionsOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final QualifiedObjectIdentifier idOfAccessControlledObject;
    protected final UUID groupId;
    protected final Set<String> actions;

    public AclPutPermissionsOperation(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId,
            Set<String> actions) {
        this.idOfAccessControlledObject = idOfAccessControlledObject;
        this.groupId = groupId;
        this.actions = actions;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAclPutPermissions(idOfAccessControlledObject, groupId, actions);
        return null;
    }
}
