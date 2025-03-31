package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class SetAccessTokenOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -4814392267645680983L;
    protected final String username;
    protected final String accessToken;

    public SetAccessTokenOperation(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalSetAccessToken(username, accessToken);
        return null;
    }
}
