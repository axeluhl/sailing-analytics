package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveRoleDefinitionFromUserGroupOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 5671806644383687490L;
    protected final UUID groupId;
    protected final UUID roleDefinitionId;

    public RemoveRoleDefinitionFromUserGroupOperation(UUID groupId, UUID roleDefinitionId) {
        this.groupId = groupId;
        this.roleDefinitionId = roleDefinitionId;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalRemoveRoleDefinitionFromUserGroup(groupId, roleDefinitionId);
        return null;
    }
}
