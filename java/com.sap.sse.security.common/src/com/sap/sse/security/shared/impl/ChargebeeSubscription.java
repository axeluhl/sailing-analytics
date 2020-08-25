package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Subscription;

public class ChargebeeSubscription extends Subscription {
    public static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";

    public static final String TRANSACTION_TYPE_PAYMENT = "payment";
    public static final String TRANSACTION_TYPE_REFUND = "refund";

    public static final String TRANSACTION_STATUS_SUCCESS = "success";

    private static final long serialVersionUID = -3682427457347116687L;

    public static Subscription createEmptySubscription(String planId, long latestEventTime, long manualUpdatedAt) {
        return new ChargebeeSubscription(null, planId, null, 0, 0, null, null, null, null, null, null, 0, 0,
                latestEventTime, manualUpdatedAt);
    }

    public ChargebeeSubscription(String subscriptionId, String planId, String customerId, long trialStart,
            long trialEnd, String subscriptionStatus, String paymentStatus, String transactionType,
            String transactionStatus, String invoiceId, String invoiceStatus, long subscriptionCreatedAt,
            long subscriptionUpdatedAt, long latestEventTime, long manualUpdatedAt) {
        super(subscriptionId, planId, customerId, trialStart, trialEnd, subscriptionStatus, paymentStatus,
                transactionType, transactionStatus, invoiceId, invoiceStatus, subscriptionCreatedAt,
                subscriptionUpdatedAt, latestEventTime, manualUpdatedAt);
    }

    public boolean isActiveSubscription() {
        String subscriptionStatus = getSubscriptionStatus();
        String paymentStatus = getPaymentStatus();
        String transactionType = getTransactionType();
        return subscriptionStatus != null && (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)
                || (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE) && paymentStatus != null
                        && paymentStatus.equals(PAYMENT_STATUS_SUCCESS) && transactionType != null
                        && transactionType.equals(TRANSACTION_TYPE_PAYMENT)));
    }
}
