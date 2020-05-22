package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

/**
 * Remote service interface for handling user subscription actions
 * 
 * @author Tu Tran
 */
public interface SubscriptionService extends RemoteService {
    /**
     * Generate checkout hosted page object. Client has to call this method to get the hosted page before opening
     * checkout modal. Reference flow of getting hosted page object from here
     * {@link https://www.chargebee.com/checkout-portal-docs/api-checkout.html#call-flow}
     * 
     * @param planId
     *            Plan id to subscribe to
     */
    public HostedPageResultDTO generateHostedPageObject(String planId);

    /**
     * Call this method from front-end after checkout is success. This method will send checkout acknowledgement request
     * to the service, then update user's subscription into database, and send back subscription information to
     * front-end for updating view.
     * 
     * @param hostedPageId
     *            a hosted page id which is result of success checkout
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
