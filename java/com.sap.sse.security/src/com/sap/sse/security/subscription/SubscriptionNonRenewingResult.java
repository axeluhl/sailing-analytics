package com.sap.sse.security.subscription;

import com.sap.sse.security.shared.subscription.Subscription;

/**
 * SubscriptionCancelResult holds result of setting subscription non renewing request
 * {@code SubscriptionApiService#nonRenewingSubscription(String)}
 */
public class SubscriptionNonRenewingResult {
    private boolean success;
    /**
     * New subscription for updating for user. In case of null then no new data for updating
     */
    private Subscription subscription;

    /**
     * Whether subscription was already deleted
     */
    private boolean deleted;

    public SubscriptionNonRenewingResult(boolean success, Subscription subscription, boolean deleted) {
        this.success = success;
        this.subscription = subscription;
        this.deleted = deleted;
    }

    public SubscriptionNonRenewingResult(boolean success, Subscription subscription) {
        this(success, subscription, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
