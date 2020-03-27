package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class ValidateEmailOperation implements SecurityOperation<Boolean> {
    private static final long serialVersionUID = 2694639274918294183L;
    protected final String username;
    protected final String validationSecret;

    public ValidateEmailOperation(String username, String validationSecret) {
        this.username = username;
        this.validationSecret = validationSecret;
    }

    @Override
    public Boolean internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalValidateEmail(username, validationSecret);
    }
}
