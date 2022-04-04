package com.sap.sse.security.subscription;

import java.util.List;
import java.util.Map;

import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiService extends SubscriptionApiBaseService {
    
    public static interface OnSubscriptionsResultListener{
        void onSubscriptionsResult(Map<String, List<Subscription>> subscriptions);
    }
    
    public static interface OnCancelSubscriptionResultListener{
        void onCancelResult(SubscriptionCancelResult cancelResult);
    }
    
    /**
     * Fetch user subscriptions from payment service provider. The logic of fetching subscriptions should be done in
     * background.
     * 
     * @param listener will be notified once result is available
     */
    void getUserSubscriptions(OnSubscriptionsResultListener listener);

    /**
     * Cancel user subscription by its {@link Subscription#getSubscriptionId() id}
     * 
     * @param listener will be notified once result is available
     */
    void cancelSubscription(String subscriptionId, OnCancelSubscriptionResultListener listener);

    /**
     * Check if the service is active
     */
    default boolean isActive() {
        return true;
    }

    SubscriptionDataHandler getDataHandler();
}
