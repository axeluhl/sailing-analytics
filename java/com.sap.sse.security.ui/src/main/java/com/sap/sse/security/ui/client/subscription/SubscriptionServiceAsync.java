package com.sap.sse.security.ui.client.subscription;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionServiceAsync<C, P> {
    public void prepareCheckout(String planId, AsyncCallback<P> data);

    public void getSubscription(AsyncCallback<SubscriptionDTO> subscription);

    public void getConfiguration(AsyncCallback<C> callback);
    
    public void getAllSubscriptionPlans(AsyncCallback<Iterable<SubscriptionPlanDTO>> callback);
    
    public void getAllSubscriptionPlansMappedById(AsyncCallback<Map<Serializable, SubscriptionPlanDTO>> callback);
    
    public void getUnlockingSubscriptionplans(WildcardPermission permission, AsyncCallback<Set<SubscriptionPlanDTO>> callback);
}
