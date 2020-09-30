package com.sap.sse.security.shared.subscription.chargebee;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionProvider;

public class ChargeeSubscriptionProvider implements SubscriptionProvider {
    private static final String PROVIDER_NAME = "chargebee";

    private static ChargeeSubscriptionProvider instance;

    public static ChargeeSubscriptionProvider getInstance() {
        if (instance == null) {
            instance = new ChargeeSubscriptionProvider();
        }
        return instance;
    }

    private ChargeeSubscriptionProvider() {
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Subscription createSubscription(String subscriptionId, String planId, String customerId,
            TimePoint trialStart, TimePoint trialEnd, String subscriptionStatus, String paymentStatus,
            String transactionType, String transactionStatus, String invoiceId, String invoiceStatus,
            TimePoint subscriptionCreatedAt, TimePoint subscriptionUpdatedAt, TimePoint latestEventTime,
            TimePoint manualUpdatedAt) {
        return new ChargebeeSubscription(subscriptionId, planId, customerId, trialStart, trialEnd, subscriptionStatus,
                paymentStatus, transactionType, transactionStatus, invoiceId, invoiceStatus, subscriptionCreatedAt,
                subscriptionUpdatedAt, latestEventTime, manualUpdatedAt);
    }
}
