package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class ResetPasswordOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -2160136068528882226L;
    protected final String username;
    protected final String passwordResetSecret;

    public ResetPasswordOperation(String username, String passwordResetSecret) {
        this.username = username;
        this.passwordResetSecret = passwordResetSecret;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalResetPassword(username, passwordResetSecret);
        return null;
    }
}
