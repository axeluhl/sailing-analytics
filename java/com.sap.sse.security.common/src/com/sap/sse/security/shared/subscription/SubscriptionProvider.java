package com.sap.sse.security.shared.subscription;

/**
 * Interface for subscription provider
 */
public interface SubscriptionProvider {
    /**
     * Return the provider's name
     */
    public String getProviderName();

    /**
     * Get subscription data handler {@code SubscriptionDataHandler} for this provider
     */
    public SubscriptionDataHandler getDataHandler();
}
