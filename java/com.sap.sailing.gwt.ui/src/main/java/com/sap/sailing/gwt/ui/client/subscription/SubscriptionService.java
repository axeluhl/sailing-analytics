package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

/**
 * Base subscription remote service interface for all payment provider services
 */
public interface SubscriptionService<P> extends RemoteService {
    /**
     * Prepare checkout for a plan, and return necessary data from payment provider for starting checkout process. This
     * is place where we will build customer data and request for checkout token from payment service provider
     */
    public P prepareCheckout(String planId);

    /**
     * Fetch user current subscription data from database
     */
    public SubscriptionDTO getSubscription();
}
