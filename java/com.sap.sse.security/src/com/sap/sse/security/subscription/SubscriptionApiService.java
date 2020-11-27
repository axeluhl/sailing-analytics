package com.sap.sse.security.subscription;

import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiService {
    /**
     * Initialize API service, such as do some SDK configurations
     */
    void initialize();

    /**
     * Return all subscriptions of user from payment service provider
     * 
     * @throws Exception
     */
    Iterable<Subscription> getUserSubscriptions(User user) throws Exception;

    /**
     * Cancel user subscription by its id
     */
    SubscriptionCancelResult cancelSubscription(String subscriptionId) throws Exception;
}
