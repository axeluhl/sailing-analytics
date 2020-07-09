package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionDTO;

/**
 * Async-interface for {@link SubscriptionService}
 * 
 * @author Tu Tran
 */
public interface SubscriptionServiceAsync {
    public void generateHostedPageObject(String planId, AsyncCallback<HostedPageResultDTO> callback);

    public void updatePlanSuccess(String hostedPageId, AsyncCallback<SubscriptionDTO> callback);

    public void getSubscription(AsyncCallback<SubscriptionDTO> callback);

    void cancelSubscription(AsyncCallback<Boolean> callback);
}
