package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveAccessTokenOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;

    public RemoveAccessTokenOperation(String username) {
        this.username = username;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalRemoveAccessToken(username);
        return null;
    }
}
