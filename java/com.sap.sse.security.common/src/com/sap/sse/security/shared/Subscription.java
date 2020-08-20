package com.sap.sse.security.shared;

import java.io.Serializable;

import com.sap.sse.security.shared.impl.User;

/**
 * Subscription data model for user which is persisted into database as subscription property of a user
 * 
 * @author tutran
 */
public abstract class Subscription implements Serializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";

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
     * Subscription trial start timestamp
     */
    private final long trialStart;

    /**
     * Subscription trial end timestamp
     */
    private final long trialEnd;

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
     * Record the creating timestamp of the subscription
     */
    private final long subscriptionCreatedAt;

    /**
     * Record the updating timestamp of the subscription
     */
    private final long subscriptionUpdatedAt;

    /**
     * Record the timestamp of the latest handled WebHook event. Because a WebHook event might be retried to send to our
     * system if it has been failed on the previous time, so we will this timestamp to only process a newer event,
     * otherwise we will update wrong data for a user.
     */
    private final long latestEventTime;

    /**
     * Record the timestamp the subscription was updated by user using the system, like changing plan or cancel
     * subscription. Reason is payment service will retried to send us failed WebHook events, so with this timestamp
     * we'll process only WebHook events occur after this timestamp, otherwise we'll update our system with old data.
     */
    private final long manualUpdatedAt;

    public Subscription(String subscriptionId, String planId, String customerId, long trialStart, long trialEnd,
            String subscriptionStatus, String paymentStatus, long subscriptionCreatedAt, long subscriptionUpdatedAt,
            long latestEventTime, long manualUpdatedAt) {
        this.subscriptionId = subscriptionId;
        this.planId = planId;
        this.customerId = customerId;
        this.trialStart = trialStart;
        this.trialEnd = trialEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
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

    public long getTrialStart() {
        return trialStart;
    }

    public long getTrialEnd() {
        return trialEnd;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public long getSubscriptionCreatedAt() {
        return subscriptionCreatedAt;
    }

    public long getSubscriptionUpdatedAt() {
        return subscriptionUpdatedAt;
    }

    public long getLatestEventTime() {
        return latestEventTime;
    }

    public long getManuallyUpdatedAt() {
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
        final String seperator = ", ";
        builder.append("subscriptionId: ").append(getStringFieldValue(subscriptionId)).append(seperator)
                .append("planId: ").append(getStringFieldValue(planId)).append(seperator).append("customerId: ")
                .append(getStringFieldValue(customerId)).append(seperator).append("subscriptionStatus: ")
                .append(getStringFieldValue(subscriptionStatus)).append(seperator).append("paymentStatus: ")
                .append(getStringFieldValue(paymentStatus)).append(seperator).append("trialStart: ").append(trialStart)
                .append(seperator).append("trialEnd: ").append(trialEnd).append(seperator).append("latestEventTime: ")
                .append(latestEventTime).append(seperator).append("manualUpdatedAt: ").append(manualUpdatedAt)
                .append(seperator).append("subscriptionCreatedAt: ").append(subscriptionCreatedAt).append(seperator)
                .append("subscriptionUpdatedAt: ").append(subscriptionUpdatedAt);

        return builder.toString();
    }

    private String getStringFieldValue(String value) {
        return (value == null || value.equals("")) ? "empty" : value;
    }
}
