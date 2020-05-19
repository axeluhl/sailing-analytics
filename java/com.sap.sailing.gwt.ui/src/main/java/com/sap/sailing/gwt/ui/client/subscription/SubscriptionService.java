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
    /**
     * Generate Chargebee checkout hosted page object. Client has to call this method to get the hosted page before
     * opening Chargebee checkout modal Check flow of getting hosted page object from here
     * {@link https://www.chargebee.com/checkout-portal-docs/api-checkout.html#call-flow}
     * 
     * @param planId
     *            Plan id to subscribe to
     */
    public HostedPageResultDTO generateHostedPageObject(String planId);

    /**
     * Call this method from frontend after checkout is success. This method will send acknowledge request to Chargebee
     * for the checkout, and update user's subscription, and send back subscription information to frontend for updating
     * view.
     * 
     * @param hostedPageId
     *            the success hosted page id which is returned from Chargebee
     */
    public SubscriptionDTO updatePlanSuccess(String hostedPageId);

    /**
     * Fetch user current subscription data from database
     */
    public SubscriptionDTO getSubscription();

    /**
     * Cancel current user subscription
     */
    public boolean cancelSubscription();
}
