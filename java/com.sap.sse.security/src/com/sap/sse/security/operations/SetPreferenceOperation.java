package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class SetPreferenceOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 7364165213456514889L;
    protected final String username;
    protected final String key;
    protected final String value;

    public SetPreferenceOperation(String username, String key, String value) {
        this.username = username;
        this.key = key;
        this.value = value;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalSetPreference(username, key, value);
        return null;
    }
}
