package com.sap.sse.security.ui.client.subscription;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

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
    public SubscriptionListDTO getSubscriptions();
    
    public ArrayList<SubscriptionPlanDTO> getAllSubscriptionPlans();
    
    public ArrayList<String> getUnlockingSubscriptionplans(WildcardPermission permission) throws UserManagementException;

    SubscriptionPlanDTO getSubscriptionPlanById(String planId);

    boolean isUserInPossessionOfRoles(String planId) throws UserManagementException;

}
