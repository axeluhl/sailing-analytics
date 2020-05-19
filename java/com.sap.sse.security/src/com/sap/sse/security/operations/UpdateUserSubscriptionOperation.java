package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.Subscription;

/**
 * Update user's subscription operation
 * 
 * @author tutran
 */
public class UpdateUserSubscriptionOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 4943500215851172841L;
    
    private String username;
    private Subscription subscription;
    
    public UpdateUserSubscriptionOperation(String username, Subscription subscription) {
        this.username = username;
        this.subscription = subscription;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateSubscription(username, subscription);
        return null;
    }

}
