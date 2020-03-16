package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class DeleteUserOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;

    public DeleteUserOperation(String username) {
        this.username = username;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalDeleteUser(username);
        return null;
    }
}
