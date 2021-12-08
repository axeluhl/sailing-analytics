package com.sap.sse.security.ui.client.subscription;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionServiceAsync<C, P> {
    public void prepareCheckout(String planId, AsyncCallback<P> data);

    public void getSubscription(AsyncCallback<SubscriptionListDTO> subscription);

    public void getConfiguration(AsyncCallback<C> callback);
    
    public void getAllSubscriptionPlans(AsyncCallback<ArrayList<SubscriptionPlanDTO>> callback);
    
    public void getUnlockingSubscriptionplans(WildcardPermission permission, AsyncCallback<ArrayList<String>> callback);
}
