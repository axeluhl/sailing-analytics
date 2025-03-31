package com.sap.sse.security.ui.client;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;

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
    private final SubscriptionServiceFactory subscriptionServiceFactory;

    public DefaultWithSecurityImpl() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath,
                HEADER_FORWARD_TO_REPLICA);
        userManagementWriteService = GWT.create(UserManagementWriteService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementWriteService,
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath,
                HEADER_FORWARD_TO_MASTER);
        userService = new UserService(userManagementService, userManagementWriteService);
        subscriptionServiceFactory = SubscriptionServiceFactory.getInstance();
        subscriptionServiceFactory.registerAsyncServices(
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.subscriptionServiceRemotePath);
    }

    @Override
    public SubscriptionServiceFactory getSubscriptionServiceFactory() {
        return subscriptionServiceFactory;
    }

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    @Override
    public UserManagementWriteServiceAsync getUserManagementWriteService() {
        return userManagementWriteService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

}
