package com.sap.sailing.gwt.ui.client.subscription;

import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface SubscriptionWriteService<P, F> extends SubscriptionService<P> {
    /**
     * Finish checkout processing for a plan with success checkout data built from payment service provider. This is
     * place where we will setup subscription for user on payment service provider, and persist user subscription data
     */
    public SubscriptionDTO finishCheckout(String planId, F checkoutData);
    
    /**
     * Cancel current user subscription
     * 
     * @param planId
     *            id of canceled plan
     * @return {@code true} if and only if canceling the subscription worked
     */
    public boolean cancelSubscription(String planId);
}
