package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class PutRoleDefinitionToUserGroupOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final UUID groupId;
    protected final UUID roleDefinitionId;
    protected final boolean forAll;

    public PutRoleDefinitionToUserGroupOperation(UUID groupId, UUID roleDefinitionId, boolean forAll) {
        this.groupId = groupId;
        this.roleDefinitionId = roleDefinitionId;
        this.forAll = forAll;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalPutRoleDefinitionToUserGroup(groupId, roleDefinitionId, forAll);
        return null;
    }
}
