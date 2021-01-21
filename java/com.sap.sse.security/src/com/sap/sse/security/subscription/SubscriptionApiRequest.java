package com.sap.sse.security.subscription;

/**
 * Subscription provider API request interface. A request needs to be queued and scheduled by
 * {@code SubscriptionApiRequestProcessor}
 */
@FunctionalInterface
public interface SubscriptionApiRequest {
    /**
     * Handle sending API request to subscription provider service
     */
    void run();
}
