package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public abstract class SubscriptionItem implements IsSerializable {
    public static final String PAYMENT_STATUS_SUCCESS = "success";
    public static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";

    /**
     * User current subscription plan id
     */
    private String planId;

    /**
     * Trial start time
     */
    private TimePoint trialStart;

    /**
     * Trial end time
     */
    private TimePoint trialEnd;

    /**
     * Subscription status: active or trial
     */
    private String subscriptionStatus;

    /**
     * Subscription payment status: {@code SubscriptionItem#PAYMENT_STATUS_SUCCESS} or
     * {@code SubscriptionItem#PAYMENT_STATUS_NO_SUCCESS}
     */
    private String paymentStatus;

    /**
     * Subscription transaction type: payment or refund
     */
    private String transactionType;

    public SubscriptionItem() {
    }

    public SubscriptionItem(String planId, TimePoint trialStart, TimePoint trialEnd, String subscriptionStatus,
            String paymentStatus, String transactionType) {
        this.planId = planId;
        this.trialStart = trialStart;
        this.trialEnd = trialEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionType = transactionType;
    }

    /**
     * Check if subscription is in trial status
     */
    public abstract boolean isInTrial();

    /**
     * Check if subscription is in active status
     */
    public abstract boolean isActive();

    public boolean isPaymentSuccess() {
        return paymentStatus != null && paymentStatus.equals(PAYMENT_STATUS_SUCCESS);
    }

    /**
     * Get subscription status i18n label
     */
    public abstract String getSubscriptionStatusLabel();

    /**
     * Get subscription payment status i18n label
     */
    public abstract String getPaymentStatusLabel();

    /**
     * Check if subscription transaction is refunded
     */
    public abstract boolean isRefunded();

    public String getPlanId() {
        return planId;
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

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public void setTrialStart(TimePoint trialStart) {
        this.trialStart = trialStart;
    }

    public void setTrialEnd(TimePoint trialEnd) {
        this.trialEnd = trialEnd;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
