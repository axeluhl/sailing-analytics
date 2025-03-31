package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class UnsetPreferenceOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 5927459056005986015L;
    protected final String username;
    protected final String key;

    public UnsetPreferenceOperation(String username, String key) {
        this.username = username;
        this.key = key;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUnsetPreference(username, key);
        return null;
    }
}
