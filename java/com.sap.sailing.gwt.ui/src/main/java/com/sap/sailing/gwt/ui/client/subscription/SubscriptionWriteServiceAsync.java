package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface SubscriptionWriteServiceAsync<C, P, F> extends SubscriptionServiceAsync<C, P> {
    public void finishCheckout(String planId, F checkoutData, AsyncCallback<SubscriptionDTO> result);

    public void cancelSubscription(String planId, AsyncCallback<Boolean> result);
}
