package com.sap.sse.security.ui.client;

import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_REPLICA;

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
    private UserManagementWriteServiceAsync userManagementWriteService;

    public DefaultWithSecurityImpl() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath,
                HEADER_FORWARD_TO_REPLICA);
        userManagementWriteService = GWT.create(UserManagementWriteService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementWriteService,
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceWriteRemotePath,
                HEADER_FORWARD_TO_MASTER);
        userService = new UserService(userManagementService, userManagementWriteService);
    }

    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    public UserManagementWriteServiceAsync getUserManagementWriteService() {
        return userManagementWriteService;
    }

    public UserService getUserService() {
        return userService;
    }
    
    public static final String sailingServiceRemotePath = "service/sailing";
    
    public static final String sailingServiceWriteRemotePath = "service/sailingmaster";
    
    
}
