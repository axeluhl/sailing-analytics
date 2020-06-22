package com.sap.sse.security.shared;

import java.io.Serializable;

/**
 * Subscription data model for user which is persisted into database as subscription property of a user
 * 
 * @author tutran
 */
public class Subscription implements Serializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";

    private static final long serialVersionUID = 96845123954667808L;

    /**
     * Subscription id
     */
    private String subscriptionId;

    /**
     * Current subscription plan id
     */
    private String planId;

    /**
     * Chargebee's customer id, this is same as the system's user name
     */
    private String customerId;

    /**
     * Subscription trial start timestamp
     */
    private long trialStart;

    /**
     * Subscription trial end timestamp
     */
    private long trialEnd;

    /**
     * Subscription status, it could be in_trial, active, or cancelled.
     */
    private String subscriptionStatus;

    /**
     * Subscription payment status, it records if user has successfully paid for the subscription. User will pay for the
     * subscription only if the subscription is turned to active(after trial period) If user has successfully paid for
     * the subscription, this has value success, otherwise no_success
     */
    private String paymentStatus;

    /**
     * Record the creating timestamp of the subscription
     */
    private long subsciptionCreatedAt;

    /**
     * Record the updating timestamp of the subscription
     */
    private long subsciptionUpdatedAt;

    /**
     * Record the timestamp of the latest handled WebHook event. Because a WebHook event might be retried to send to our
     * system if it has been failed on the previous time, so we will this timestamp to only process a newer event,
     * otherwise we will update wrong data for a user.
     * 
     * {@link https://www.chargebee.com/docs/webhook_settings.html#automatic-retries}
     */
    private long latestEventTime;

    /**
     * Record the timestamp the subscription was updated by user using the system, like changing plan or cancel
     * subscription. Reason is payment service will retried to send us failed WebHook events, so with this timestamp
     * we'll process only WebHook events occur after this timestamp, otherwise we'll update our system with old data.
     */
    private long manualUpdatedAt;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getTrialStart() {
        return trialStart;
    }

    public void setTrialStart(long trialStart) {
        this.trialStart = trialStart;
    }

    public long getTrialEnd() {
        return trialEnd;
    }

    public void setTrialEnd(long trialEnd) {
        this.trialEnd = trialEnd;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getSubsciptionCreatedAt() {
        return subsciptionCreatedAt;
    }

    public void setSubsciptionCreatedAt(long subsciptionCreatedAt) {
        this.subsciptionCreatedAt = subsciptionCreatedAt;
    }

    public long getSubsciptionUpdatedAt() {
        return subsciptionUpdatedAt;
    }

    public void setSubsciptionUpdatedAt(long subsciptionUpdatedAt) {
        this.subsciptionUpdatedAt = subsciptionUpdatedAt;
    }

    public long getLatestEventTime() {
        return latestEventTime;
    }

    public void setLatestEventTime(long latestEventTime) {
        this.latestEventTime = latestEventTime;
    }

    public long getManuallyUpdatedAt() {
        return manualUpdatedAt;
    }

    public void setManualUpdatedAt(long manualUpdatedAt) {
        this.manualUpdatedAt = manualUpdatedAt;
    }

    /**
     * Check if subscription is active, base on this user will gain roles for the subscription
     * 
     * @return true if status subscription is in trial or status is active and user has success payment
     */
    public boolean isActiveSubscription() {
        return subscriptionStatus != null && (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)
                || (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE) && paymentStatus != null
                        && paymentStatus.equals(PAYMENT_STATUS_SUCCESS)));
    }
}
