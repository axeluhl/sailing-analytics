package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;

/**
 * User subscription data transfer object {@link SubscriptionService}
 * 
 * @author tutran
 */
public class SubscriptionDTO implements IsSerializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";

    /**
     * User current subscription plan id
     */
    public String planId;

    /**
     * Trial start timestamp
     */
    public long trialStart;

    /**
     * Trial end timestamp
     */
    public long trialEnd;

    /**
     * Subscription status: active or in_trial
     */
    public String subscriptionStatus;

    /**
     * Subscription payment status: success or no_success
     */
    public String paymentStatus;

    /**
     * Error message
     */
    public String error;

    /**
     * Check if subscription is in trial status
     * 
     * @return
     */
    public boolean isInTrial() {
        return subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL);
    }

    /**
     * Check if subscription is in active status
     * 
     * @return
     */
    public boolean isActive() {
        return subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE);
    }

    public boolean isPaymentSuccess() {
        return paymentStatus.equals(PAYMENT_STATUS_SUCCESS);
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
}
