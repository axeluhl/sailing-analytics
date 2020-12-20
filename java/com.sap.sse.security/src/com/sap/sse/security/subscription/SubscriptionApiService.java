package com.sap.sse.security.subscription;

import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiService {
    /**
     * The OSGi registry property name under which to look up a specific implementation of this API.
     */
    String PROVIDER_NAME_OSGI_REGISTRY_KEY = "provider-name";
    
    /**
     * The name used for the {@link #PROVIDER_NAME_OSGI_REGISTRY_KEY} key during registration of this
     * service with the OSGi service registry.
     */
    String getProviderName();

    /**
     * Return all subscriptions of user from payment service provider
     * 
     * @throws Exception
     */
    Iterable<Subscription> getUserSubscriptions(User user) throws Exception;

    /**
     * Cancel user subscription by its {@link Subscription#getSubscriptionId() id}
     */
    SubscriptionCancelResult cancelSubscription(String subscriptionId) throws Exception;
    
    /**
     * Check if the service is active
     */
    default boolean isActive() {
        return true;
    }

    SubscriptionDataHandler getDataHandler();
}
