package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionService;

/**
 * User subscription data transfer object {@link SubscriptionService#getSubscription()}
 * 
 * @author Tu Tran
 */
public class SubscriptionDTO implements IsSerializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";

    /**
     * User current subscription plan id
     */
    private String planId;

    /**
     * Trial start timestamp
     */
    private long trialStart;

    /**
     * Trial end timestamp
     */
    private long trialEnd;

    /**
     * Subscription status: active or in_trial
     */
    private String subscriptionStatus;

    /**
     * Subscription payment status: success or no_success
     */
    private String paymentStatus;

    /**
     * Error message
     */
    private String error;

    /**
     * Check if subscription is in trial status
     * 
     * @return
     */
    public boolean isInTrial() {
        return subscriptionStatus != null && subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL);
    }

    /**
     * Check if subscription is in active status
     * 
     * @return
     */
    public boolean isActive() {
        return subscriptionStatus != null && subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE);
    }

    public boolean isPaymentSuccess() {
        return paymentStatus != null && paymentStatus.equals(PAYMENT_STATUS_SUCCESS);
    }

    /**
     * Get subscription status i18n label
     * 
     * @return
     */
    public String getSubscriptionStatusLabel() {
        if (subscriptionStatus != null) {
            if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)) {
                return StringMessages.INSTANCE.inTrial();
            } else if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE)) {
                return StringMessages.INSTANCE.active();
            }
        }

        return "";
    }

    /**
     * Get subscription payment status i18n label
     * 
     * @return
     */
    public String getPaymentStatusLabel() {
        if (paymentStatus != null) {
            if (paymentStatus.equals(PAYMENT_STATUS_SUCCESS)) {
                return StringMessages.INSTANCE.paymentStatusSuccess();
            } else {
                return StringMessages.INSTANCE.paymentStatusNoSuccess();
            }
        }

        return "";
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
