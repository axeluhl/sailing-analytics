package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.EntryPointHelper;

/**
 * The default implementation for the WithSecurity interface
 * Adds user management service and user service for security management.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class DefaultWithSecurityImpl implements WithSecurity {
    private UserService userService;
    private UserManagementServiceAsync userManagementService;

    public DefaultWithSecurityImpl() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
    }
    
    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }
    
    public UserService getUserService() {
        return userService;
    }

}
