package com.sap.sse.security.subscription;

import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiService {
    /**
     * Return all subscriptions of user from payment service provider
     * 
     * @throws Exception
     */
    Iterable<Subscription> getUserSubscriptions(User user) throws Exception;

    /**
     * Cancel user subscription by its id
     * 
     * @return New subscription model that would be used for updating user's subscriptions
     */
    Subscription cancelSubscription(String subscriptionId) throws Exception;
}
