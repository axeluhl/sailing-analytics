package com.sap.sailing.gwt.ui.client.subscription;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlanDTO;

/**
 * Base subscription remote service interface for all payment provider services
 */
public interface SubscriptionService<C, P> extends RemoteService {
    /**
     * Return provider configuration that will be required for client to setup
     */
    public C getConfiguration();
    
    /**
     * Prepare checkout for a plan, and return necessary data from payment provider for starting checkout process. This
     * is place where we will build customer data and request for checkout token from payment service provider
     */
    public P prepareCheckout(String planId);

    /**
     * Fetch user current subscription data from database
     */
    public SubscriptionDTO getSubscription();
    
    public Iterable<SubscriptionPlanDTO> getAllSubscriptionPlans();
    
    public Map<Serializable, SubscriptionPlanDTO> getAllSubscriptionPlansMappedById();

}
