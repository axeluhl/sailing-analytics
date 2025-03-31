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
public interface SubscriptionService extends RemoteService {
    /**
     * Fetch user current subscription data from database
     */
    public SubscriptionListDTO getSubscriptions(Boolean activeOnly);

    public ArrayList<SubscriptionPlanDTO> getAllSubscriptionPlans();

    public ArrayList<String> getUnlockingSubscriptionplans(WildcardPermission permission) throws UserManagementException;

    SubscriptionPlanDTO getSubscriptionPlanDTOById(String planId);

    boolean isUserInPossessionOfRoles(String planId) throws UserManagementException;
    
    String getSelfServicePortalSession();
}
