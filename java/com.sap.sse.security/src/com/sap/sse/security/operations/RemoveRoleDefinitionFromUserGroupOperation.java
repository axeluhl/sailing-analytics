package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveRoleDefinitionFromUserGroupOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

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
