package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class UpdateSimpleUserEmailOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 2011752812733501341L;
    protected final String username;
    protected final String newEmail;
    protected final String validationSecret;

    public UpdateSimpleUserEmailOperation(String username, String newEmail, String validationSecret) {
        this.username = username;
        this.newEmail = newEmail;
        this.validationSecret = validationSecret;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateSimpleUserEmail(username, newEmail, validationSecret);
        return null;
    }
}
