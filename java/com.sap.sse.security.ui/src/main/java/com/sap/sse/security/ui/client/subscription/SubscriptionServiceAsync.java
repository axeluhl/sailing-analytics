package com.sap.sse.security.ui.client.subscription;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionServiceAsync<C, P> {

    public void getSubscriptions(Boolean activeOnly, AsyncCallback<SubscriptionListDTO> subscription);

    public void getAllSubscriptionPlans(AsyncCallback<ArrayList<SubscriptionPlanDTO>> callback);
    
    public void getUnlockingSubscriptionplans(WildcardPermission permission, AsyncCallback<ArrayList<String>> callback);

    public void getSubscriptionPlanDTOById(String planId, AsyncCallback<SubscriptionPlanDTO> callback);
    
    void isUserInPossessionOfRoles(String planId, AsyncCallback<Boolean> callback);
    
    void getSelfServicePortalSession(AsyncCallback<String> accessLink);

}
