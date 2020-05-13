package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SubscriptionDTO implements IsSerializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";
    
    public String planId;
    public long trialStart;
    public long trialEnd;
    public String subscriptionStatus;
    public String paymentStatus;
    public String error;
    
    public boolean isInTrial() {
        return subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL);
    }
    
    public boolean isActive() {
        return subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE);
    }
    
    public boolean isPaymentSuccess() {
        return paymentStatus.equals(PAYMENT_STATUS_SUCCESS);
    }
    
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
