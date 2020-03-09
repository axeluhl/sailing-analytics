package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveUserFromUserGroupOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final UUID groupId;
    protected final String username;

    public RemoveUserFromUserGroupOperation(UUID groupId, String username) {
        this.groupId = groupId;
        this.username = username;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalRemoveUserFromUserGroup(groupId, username);
        return null;
    }
}
