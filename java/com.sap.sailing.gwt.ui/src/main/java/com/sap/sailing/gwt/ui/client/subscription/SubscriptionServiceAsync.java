package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface SubscriptionServiceAsync<P, F> {
    public void prepareCheckout(String planId, AsyncCallback<P> data);
    public void finishCheckout(String planId, F checkoutData, AsyncCallback<SubscriptionDTO> result);
    public void getSubscription(AsyncCallback<SubscriptionDTO> subscription);
    public void cancelSubscription(String planId, AsyncCallback<Boolean> result);
}
