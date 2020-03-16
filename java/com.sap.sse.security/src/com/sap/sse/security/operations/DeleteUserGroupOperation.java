package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class DeleteUserGroupOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final UUID groupId;

    public DeleteUserGroupOperation(UUID groupId) {
        this.groupId = groupId;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalDeleteUserGroup(groupId);
        return null;
    }
}
