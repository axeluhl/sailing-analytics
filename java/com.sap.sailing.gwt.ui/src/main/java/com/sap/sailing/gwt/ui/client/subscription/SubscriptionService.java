package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

/**
 * Base subscription remote service interface for all payment provider services
 */
public interface SubscriptionService<P, F> extends RemoteService {
    /**
     * Prepare checkout for a plan, and return necessary data for starting checkout process. This is place where we will
     * build customer data and request for checkout token from payment service provider
     */
    public P prepareCheckout(String planId);

    /**
     * Finish checkout processing for a plan with success checkout data built from payment service provider. This is
     * place where we will setup subscription for user on payment service provider, and persist user subscription data
     */
    public SubscriptionDTO finishCheckout(String planId, F checkoutData);

    /**
     * Fetch user current subscription data from database
     */
    public SubscriptionDTO getSubscription();

    /**
     * Cancel current user subscription
     * 
     * @param planId
     *            id of canceled plan
     * @return {@code true} if and only if canceling the subscription worked
     */
    public boolean cancelSubscription(String planId);
}
