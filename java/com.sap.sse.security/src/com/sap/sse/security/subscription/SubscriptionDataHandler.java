package com.sap.sse.security.subscription;

import java.util.Map;

import com.sap.sse.security.shared.subscription.Subscription;

/**
 * SubscriptionDataHandler is used for persisting, and restoring subscription object data.
 * Each subscription provider has to provide an implementation of this data handler {@code SubscriptionProvider#getDataHandler()}
 */
public interface SubscriptionDataHandler {
    /**
     * Serialize subscription into data map
     */
    Map<String, Object> toMap(Subscription subscription);
    
    /**
     * Restore subscription from data
     */
    Subscription toSubscription(SubscriptionData data);
}
