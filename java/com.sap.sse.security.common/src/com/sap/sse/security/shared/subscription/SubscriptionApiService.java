package com.sap.sse.security.shared.subscription;

import com.sap.sse.security.shared.impl.User;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiService {
    /**
     * Return all subscriptions of user from provider
     * @throws Exception 
     */
    Iterable<Subscription> getUserSubscriptions(User user) throws Exception;
}
