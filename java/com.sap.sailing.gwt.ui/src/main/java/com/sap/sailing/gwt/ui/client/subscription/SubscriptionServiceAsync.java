package com.sap.sailing.gwt.ui.client.subscription;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionServiceAsync<C, P> {
    public void prepareCheckout(String planId, AsyncCallback<P> data);

    public void getSubscription(AsyncCallback<SubscriptionDTO> subscription);

    public void getConfiguration(AsyncCallback<C> callback);
    
    public void getAllSubscriptionPlans(AsyncCallback<Iterable<SubscriptionPlanDTO>> callback);
    
    public void getAllSubscriptionPlansMappedById(AsyncCallback<Map<Serializable, SubscriptionPlanDTO>> callback);
}
