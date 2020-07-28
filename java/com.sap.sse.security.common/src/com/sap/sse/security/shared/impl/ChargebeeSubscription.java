package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Subscription;

public class ChargebeeSubscription extends Subscription {
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";

    private static final long serialVersionUID = -3682427457347116687L;

    public static Subscription createEmptySubscription(String planId, long latestEventTime, long manualUpdatedAt) {
        return new ChargebeeSubscription(null, planId, null, 0, 0, null, null, 0, 0, latestEventTime, manualUpdatedAt);
    }

    public ChargebeeSubscription(String subscriptionId, String planId, String customerId, long trialStart,
            long trialEnd, String subscriptionStatus, String paymentStatus, long subscriptionCreatedAt,
            long subscriptionUpdatedAt, long latestEventTime, long manualUpdatedAt) {
        super(subscriptionId, planId, customerId, trialStart, trialEnd, subscriptionStatus, paymentStatus,
                subscriptionCreatedAt, subscriptionUpdatedAt, latestEventTime, manualUpdatedAt);
    }

    public boolean isActiveSubscription() {
        String subscriptionStatus = getSubscriptionStatus();
        String paymentStatus = getPaymentStatus();
        return subscriptionStatus != null && (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)
                || (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE) && paymentStatus != null
                        && paymentStatus.equals(PAYMENT_STATUS_SUCCESS)));
    }
}
