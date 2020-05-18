package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

/**
 * Remote service interface for handling user subscription actions
 * 
 * @author tutran
 */
public interface SubscriptionService extends RemoteService {
    public HostedPageResultDTO generateHostedPageObject(String planId);
    public SubscriptionDTO updatePlanSuccess(String hostedPageId);
    public SubscriptionDTO getSubscription();
    public boolean cancelSubscription();
}
