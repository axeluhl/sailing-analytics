package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FinishCheckoutDTO implements IsSerializable {
    private String hostedPageId;

    public String getHostedPageId() {
        return hostedPageId;
    }

    public void setHostedPageId(String hostedPageId) {
        this.hostedPageId = hostedPageId;
    }
}
