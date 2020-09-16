package com.sap.sse.security.shared;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.impl.User;

/**
 * Subscription data model for a {@link User} which is stored persistently together with the {@link User} object. See,
 * e.g., {@link User#setSubscriptions(Subscription[])}.
 * 
 * @author tutran
 */
public abstract class Subscription implements Serializable {
    public static final String PAYMENT_STATUS_SUCCESS = "success";
    public static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";

    public static TimePoint emptyTime() {
        return getTime(0);
    }

    public static TimePoint getTime(long milisTimestamp) {
        return TimePoint.of(milisTimestamp);
    }

    private static final long serialVersionUID = 96845123954667808L;

    /**
     * Subscription id
     */
    private final String subscriptionId;

    /**
     * Current subscription plan id
     */
    private final String planId;

    /**
     * Chargebee's customer id, taken from {@link User#getName()}
     */
    private final String customerId;

    /**
     * Subscription trial start time
     */
    private final TimePoint trialStart;

    /**
     * Subscription trial end time
     */
    private final TimePoint trialEnd;

    /**
     * Subscription status, it could be trial, active, or cancelled.
     */
    private final String subscriptionStatus;

    /**
     * Subscription payment status, it records if user has successfully paid for the subscription. User will pay for the
     * subscription only if the subscription is turned to active(after trial period) If user has successfully paid for
     * the subscription, this has value success, otherwise no_success
     */
    private final String paymentStatus;

    /**
     * Subscription latest invoice id
     */
    private final String invoiceId;

    /**
     * Subscription latest invoice status
     */
    private final String invoiceStatus;

    /**
     * Subscription transaction type
     */
    private final String transactionType;

    /**
     * Subscription transaction status
     */
    private final String transactionStatus;

    /**
     * Record the creating time of the subscription
     */
    private final TimePoint subscriptionCreatedAt;

    /**
     * Record the updating time of the subscription
     */
    private final TimePoint subscriptionUpdatedAt;

    /**
     * Record the time of the latest handled WebHook event. Because a WebHook event might be retried to send to our
     * system if it has been failed on the previous time, so we will this time to only process a newer event, otherwise
     * we will update wrong data for a user.
     */
    private final TimePoint latestEventTime;

    /**
     * Record the time the subscription was updated by user using the system, like changing plan or cancel subscription.
     * Reason is payment service will retried to send us failed WebHook events, so with this time we'll process only
     * WebHook events occur after this time, otherwise we'll update our system with old data.
     */
    private final TimePoint manualUpdatedAt;

    public Subscription(String subscriptionId, String planId, String customerId, TimePoint trialStart,
            TimePoint trialEnd, String subscriptionStatus, String paymentStatus, String transactionType,
            String transactionStatus, String invoiceId, String invoiceStatus, TimePoint subscriptionCreatedAt,
            TimePoint subscriptionUpdatedAt, TimePoint latestEventTime, TimePoint manualUpdatedAt) {
        this.subscriptionId = subscriptionId;
        this.planId = planId;
        this.customerId = customerId;
        this.trialStart = trialStart;
        this.trialEnd = trialEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.invoiceId = invoiceId;
        this.invoiceStatus = invoiceStatus;
        this.subscriptionCreatedAt = subscriptionCreatedAt;
        this.subscriptionUpdatedAt = subscriptionUpdatedAt;
        this.latestEventTime = latestEventTime;
        this.manualUpdatedAt = manualUpdatedAt;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public TimePoint getTrialStart() {
        return trialStart;
    }

    public TimePoint getTrialEnd() {
        return trialEnd;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public TimePoint getSubscriptionCreatedAt() {
        return subscriptionCreatedAt;
    }

    public TimePoint getSubscriptionUpdatedAt() {
        return subscriptionUpdatedAt;
    }

    public TimePoint getLatestEventTime() {
        return latestEventTime;
    }

    public TimePoint getManualUpdatedAt() {
        return manualUpdatedAt;
    }

    public boolean hasPlan() {
        return planId != null && !planId.isEmpty();
    }

    /**
     * Check if subscription is active, base on this user will gain roles for the subscription
     * 
     * @return true if status subscription is in trial or status is active and user has success payment
     */
    public abstract boolean isActiveSubscription();

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final String separator = ", ";
        builder.append("subscriptionId: ").append(getStringFieldValue(subscriptionId)).append(separator)
                .append("planId: ").append(getStringFieldValue(planId)).append(separator).append("customerId: ")
                .append(getStringFieldValue(customerId)).append(separator).append("subscriptionStatus: ")
                .append(getStringFieldValue(subscriptionStatus)).append(separator).append("paymentStatus: ")
                .append(getStringFieldValue(paymentStatus)).append(separator).append("transactionType: ")
                .append(getStringFieldValue(transactionType)).append(separator).append("transactionStatus: ")
                .append(getStringFieldValue(transactionStatus)).append(separator).append("trialStart: ")
                .append(trialStart.asMillis()).append(separator).append("trialEnd: ").append(trialEnd.asMillis())
                .append(separator).append("invoiceId: ").append(getStringFieldValue(invoiceId)).append(separator)
                .append("invoiceStatus: ").append(getStringFieldValue(invoiceStatus)).append(separator)
                .append("latestEventTime: ").append(latestEventTime.asMillis()).append(separator)
                .append("manualUpdatedAt: ").append(manualUpdatedAt.asMillis()).append(separator)
                .append("subscriptionCreatedAt: ").append(subscriptionCreatedAt.asMillis()).append(separator)
                .append("subscriptionUpdatedAt: ").append(subscriptionUpdatedAt.asMillis());
        return builder.toString();
    }

    private String getStringFieldValue(String value) {
        return (value == null || value.equals("")) ? "empty" : value;
    }
}
