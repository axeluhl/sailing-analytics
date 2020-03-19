package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class AclRemovePermissionOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -252209335709805836L;
    protected final QualifiedObjectIdentifier idOfAccessControlledObject;
    protected final UUID groupId;
    protected final String action;

    public AclRemovePermissionOperation(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId,
            String action) {
        this.idOfAccessControlledObject = idOfAccessControlledObject;
        this.groupId = groupId;
        this.action = action;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAclRemovePermission(idOfAccessControlledObject, groupId, action);
        return null;
    }
}
