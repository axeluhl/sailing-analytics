package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;

public class SubscriptionItem implements IsSerializable {
    public static final String PAYMENT_STATUS_SUCCESS = "success";
    public static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String SUBSCRIPTION_STATUS_PAUSED = "paused";
    public static final String TRANSACTION_TYPE_REFUND = "refund";

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
     * Subscription status: active or in_trial
     */
    private String subscriptionStatus;

    /**
     * Subscription payment status: success or no_success
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
    public boolean isInTrial() {
        return subscriptionStatus != null && subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL);
    }

    /**
     * Check if subscription is in active status
     */
    public boolean isActive() {
        return subscriptionStatus != null && subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE);
    }

    public boolean isPaymentSuccess() {
        return paymentStatus != null && paymentStatus.equals(PAYMENT_STATUS_SUCCESS);
    }

    /**
     * Get subscription status i18n label
     */
    public String getSubscriptionStatusLabel() {
        String label = "";
        if (subscriptionStatus != null) {
            if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)) {
                label = StringMessages.INSTANCE.inTrial();
            } else if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE)) {
                label = StringMessages.INSTANCE.active();
            } else if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_PAUSED)) {
                label = StringMessages.INSTANCE.paused();
            }
        }
        return label;
    }

    /**
     * Get subscription payment status i18n label
     */
    public String getPaymentStatusLabel() {
        final String label;
        if (paymentStatus != null) {
            if (paymentStatus.equals(PAYMENT_STATUS_SUCCESS)) {
                if (isRefunded()) {
                    label = StringMessages.INSTANCE.refunded();
                } else {
                    label = StringMessages.INSTANCE.paymentStatusSuccess();
                }
            } else {
                label = StringMessages.INSTANCE.paymentStatusNoSuccess();
            }
        } else {
            label = "";
        }
        return label;
    }

    /**
     * Check if subscription transaction is refunded
     */
    public boolean isRefunded() {
        return transactionType != null && transactionType.equals(TRANSACTION_TYPE_REFUND);
    }

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
