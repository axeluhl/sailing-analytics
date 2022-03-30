package com.sap.sse.security.ui.client.subscription;

import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;

public interface SubscriptionWriteService<C, P, F> extends SubscriptionService {
    
    /**
     * Prepare checkout for a plan, and return necessary data from payment provider for starting checkout process. This
     * is place where we will build customer data and request for checkout token from payment service provider
     */
    public P prepareCheckout(String planId);
    
    /**
     * Finish checkout processing for a plan with success checkout data built from payment service provider. This is
     * place where we will setup subscription for user on payment service provider, and persist user subscription data
     */
    public SubscriptionListDTO finishCheckout(F checkoutData);
    
    /**
     * Cancel current user subscription
     * 
     * @param planId
     *            id of canceled plan
     * @return {@code true} if and only if canceling the subscription worked
     */
    public boolean cancelSubscription(String planId);
    
    boolean isMailVerificationRequired();
    
    /**
     * Return provider configuration that will be required for client to setup
     */
    public C getConfiguration();
}
