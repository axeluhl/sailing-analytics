package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

/**
 * Remove user subscriptions of a provider operation
 */
public class RemoveProviderSubscriptionsOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -3121534374405832205L;

    private String username;
    private String providerName;

    public RemoveProviderSubscriptionsOperation(String username, String providerName) {
        this.username = username;
        this.providerName = providerName;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalRemoveProviderSubscriptions(username, providerName);
        return null;
    }
}
