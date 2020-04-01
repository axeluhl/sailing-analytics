package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class UpdateSimpleUserPasswordOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -3385425219743404833L;
    protected final String username;
    protected final byte[] salt;
    protected final String hashedPasswordBase64;

    public UpdateSimpleUserPasswordOperation(String username, byte[] salt, String hashedPasswordBase64) {
        this.username = username;
        this.salt = salt;
        this.hashedPasswordBase64 = hashedPasswordBase64;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateSimpleUserPassword(username, salt, hashedPasswordBase64);
        return null;
    }
}
