package com.sap.sse.security.shared;

import java.io.Serializable;

public class Subscription implements Serializable {
    private static final long serialVersionUID = 96845123954667808L;
    
    public String hostedPageId;
    public String subscriptionId;
    public String planId;
    public String customerId;
    public long trialStart;
    public long trialEnd;
    public String transactionStatus;
    public long subsciptionCreatedAt;
    public long subsciptionUpdatedAt;
}
