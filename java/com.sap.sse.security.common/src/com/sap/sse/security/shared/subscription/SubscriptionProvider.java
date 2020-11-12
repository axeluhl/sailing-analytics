package com.sap.sse.security.shared.subscription;

/**
 * Interface for subscription provider
 */
public interface SubscriptionProvider {
    /**
     * Return the provider's name
     */
    String getProviderName();

    /**
     * Get subscription data handler {@code SubscriptionDataHandler} for this provider
     */
    SubscriptionDataHandler getDataHandler();
    
    /**
     * Return implementation of {@code SubscriptionApiService} for this provider
     */
    SubscriptionApiService getApiService();
}
