package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class DeleteUserOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 5649827118311992371L;
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
