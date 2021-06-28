package com.sap.sse.security.ui.client;

import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;

/**
 * A marker interface indicating that usermanagement and security services are needed 
 * @author Frank
 *
 */
public interface WithSecurity {
    public UserManagementServiceAsync getUserManagementService();
    
    public UserManagementWriteServiceAsync getUserManagementWriteService();
    
    public UserService getUserService();
    
    public SubscriptionServiceFactory getSubscriptionServiceFactory();
}
