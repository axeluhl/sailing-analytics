package com.sap.sse.security.shared.subscription;

import com.sap.sse.common.TimePoint;

/**
 * Interface for subscription provider
 */
public interface SubscriptionProvider {
    /**
     * Return the provider's name
     */
    public String getProviderName();
    
    /**
     * Create and return concrete subscription model for this provider
     */
    public Subscription createSubscription(String subscriptionId, String planId, String customerId, TimePoint trialStart,
            TimePoint trialEnd, String subscriptionStatus, String paymentStatus, String transactionType,
            String transactionStatus, String invoiceId, String invoiceStatus, TimePoint subscriptionCreatedAt,
            TimePoint subscriptionUpdatedAt, TimePoint latestEventTime, TimePoint manualUpdatedAt);
}
