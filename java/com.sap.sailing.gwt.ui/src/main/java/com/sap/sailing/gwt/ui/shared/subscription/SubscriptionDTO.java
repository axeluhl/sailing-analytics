package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SubscriptionDTO implements IsSerializable {
    public String planId;
    public long trialStart;
    public long trialEnd;
    public String transactionStatus;
    public String error;
}
