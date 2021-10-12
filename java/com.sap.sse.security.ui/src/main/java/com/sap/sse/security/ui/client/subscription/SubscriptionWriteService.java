package com.sap.sse.security.ui.client.subscription;

import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;

public interface SubscriptionWriteService<C, P, F> extends SubscriptionService<C, P> {
    /**
     * Finish checkout processing for a plan with success checkout data built from payment service provider. This is
     * place where we will setup subscription for user on payment service provider, and persist user subscription data
     */
    public SubscriptionListDTO finishCheckout(String planId, F checkoutData);
    
    /**
     * Cancel current user subscription
     * 
     * @param planId
     *            id of canceled plan
     * @return {@code true} if and only if canceling the subscription worked
     */
    public boolean cancelSubscription(String planId);
}
