package com.sap.sse.security.shared.subscription.chargebee;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.Subscription;

public class ChargebeeSubscription extends Subscription {
    private static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    private static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";
    public static final String SUBSCRIPTION_NON_RENEWING = "non_renewing";
    protected static final String SUBSCRIPTION_STATUS_PAUSED = "paused";

    public static final String TRANSACTION_TYPE_PAYMENT = "payment";
    protected static final String TRANSACTION_TYPE_REFUND = "refund";

    public static final String INVOICE_STATUS_PAID = "paid";

    public static final String TRANSACTION_STATUS_SUCCESS = "success";

    private static final long serialVersionUID = -3682427457347116687L;

    public static Subscription createEmptySubscription(String planId, TimePoint latestEventTime,
            TimePoint manualUpdatedAt) {
        return new ChargebeeSubscription(null, planId, null, Subscription.emptyTime(), Subscription.emptyTime(), null,
                null, null, null, null, null, null, null, Subscription.emptyTime(), Subscription.emptyTime(),
                Subscription.emptyTime(), Subscription.emptyTime(), Subscription.emptyTime(), Subscription.emptyTime(),
                latestEventTime, manualUpdatedAt);
    }

    /**
     * Determining payment status, return {@code Subscription#PAYMENT_STATUS_SUCCESS} or
     * {@code Subscription#PAYMENT_STATUS_NO_SUCCESS}
     */
    public static String determinePaymentStatus(String transactionType, String transactionStatus,
            String invoiceStatus) {
        String paymentStatus = null;
        if (transactionStatus == null) {
            if (invoiceStatus != null) {
                paymentStatus = determinePaymentStatusFromInvoiceStatus(invoiceStatus);
            }
        } else {
            if (transactionType != null && transactionType.equals(TRANSACTION_TYPE_PAYMENT)) {
                paymentStatus = determinePaymentStatusFromTransactionStatus(transactionStatus);
            }
        }
        return paymentStatus;
    }

    /**
     * If invoice is paid then payment status will be {@code Subscription#PAYMENT_STATUS_SUCCESS}, and
     * {@code Subscription#PAYMENT_STATUS_NO_SUCCESS} otherwise
     */
    public static String determinePaymentStatusFromInvoiceStatus(String invoiceStatus) {
        return invoiceStatus.equals(INVOICE_STATUS_PAID) ? Subscription.PAYMENT_STATUS_SUCCESS
                : Subscription.PAYMENT_STATUS_NO_SUCCESS;
    }

    /**
     * If transaction is success then payment status will be {@code Subscription#PAYMENT_STATUS_SUCCESS}, and
     * {@code Subscription#PAYMENT_STATUS_NO_SUCCESS} otherwise
     */
    public static String determinePaymentStatusFromTransactionStatus(String transactionStatus) {
        return transactionStatus.equals(ChargebeeSubscription.TRANSACTION_STATUS_SUCCESS)
                ? Subscription.PAYMENT_STATUS_SUCCESS
                : Subscription.PAYMENT_STATUS_NO_SUCCESS;
    }

    public ChargebeeSubscription(String subscriptionId, String planId, String customerId, TimePoint trialStart,
            TimePoint trialEnd, String subscriptionStatus, String paymentStatus, String transactionType,
            String transactionStatus, String invoiceId, String invoiceStatus, Integer reoccuringPaymentValue, String currencyCode,
            TimePoint subscriptionCreatedAt, TimePoint subscriptionUpdatedAt, TimePoint subscriptionActivatedAt,
            TimePoint nextBillingAt, TimePoint currentTermEnd, TimePoint cancelledAt, TimePoint latestEventTime,
            TimePoint manualUpdatedAt) {
        super(subscriptionId, planId, customerId, trialStart, trialEnd, subscriptionStatus, paymentStatus,
                transactionType, transactionStatus, invoiceId, invoiceStatus, reoccuringPaymentValue, currencyCode,
                subscriptionCreatedAt, subscriptionUpdatedAt, subscriptionActivatedAt, nextBillingAt, currentTermEnd, 
                cancelledAt, latestEventTime, manualUpdatedAt, ChargebeeSubscriptionProvider.PROVIDER_NAME);
    }

    @Override
    public boolean isActiveSubscription() {
        String subscriptionStatus = getSubscriptionStatus();
        return subscriptionStatus != null && (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)
                || subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE)
                || subscriptionStatus.equals(SUBSCRIPTION_NON_RENEWING));
    }

    @Override
    public void patchTransactionData(Subscription subscription) {
        if(subscription.getTransactionStatus() != null) {
            transactionStatus = subscription.getTransactionStatus();
        }
        if(subscription.getTransactionType() != null) {
            transactionType = subscription.getTransactionType();
        }
        paymentStatus = determinePaymentStatus(transactionType, transactionStatus, invoiceStatus);
    }

    @Override
    public void patchInvoiceData(Subscription subscription) {
        if(subscription.getInvoiceId() != null) {
            invoiceId = subscription.getInvoiceId();
        }
        if(subscription.getInvoiceStatus() != null) {
            invoiceStatus = subscription.getInvoiceStatus();
        }
        paymentStatus = determinePaymentStatus(transactionType, transactionStatus, invoiceStatus);
    }
}
