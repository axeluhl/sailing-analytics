package com.sap.sse.security.ui.client;

/**
 * A marker interface indicating that usermanagement and security services are needed 
 * @author Frank
 *
 */
public interface WithSecurity {
    public UserManagementServiceAsync getUserManagementService();
    
    public UserService getUserService();
}
