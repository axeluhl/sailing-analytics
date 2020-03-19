package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveAccessTokenOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 1555085165323358858L;
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
