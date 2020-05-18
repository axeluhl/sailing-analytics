package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface SubscriptionServiceAsync {
    public void generateHostedPageObject(String planId, AsyncCallback<HostedPageResultDTO> callback);
    public void updatePlanSuccess(String hostedPageId, AsyncCallback<SubscriptionDTO> callback);
    public void getSubscription(AsyncCallback<SubscriptionDTO> callback);
    void cancelSubscription(AsyncCallback<Boolean> callback);
}
