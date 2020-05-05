package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface SubscriptionService extends RemoteService {
    public String generateHostedPageObject();
    public SubscriptionDTO upgradePlanSuccess(String hostedPageId);
    public SubscriptionDTO getSubscription();
}
