package com.sap.sse.security.shared;

import java.io.Serializable;

public class Subscription implements Serializable {
    public static String PAYMENT_STATUS_SUCCESS = "success";
    public static String PAYMENT_STATUS_NO_SUCCESS = "no_success";
    public static String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";
    
    private static final long serialVersionUID = 96845123954667808L;
    
    public String subscriptionId;
    public String planId;
    public String customerId;
    public long trialStart;
    public long trialEnd;
    public String subscriptionStatus;
    public String paymentStatus;
    public long subsciptionCreatedAt;
    public long subsciptionUpdatedAt;
    public long latestEventTime;
    public long manualUpdatedAt;
}
