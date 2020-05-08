package com.sap.sse.security.operations;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class AclPutPermissionsOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 9040422347176756428L;
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
