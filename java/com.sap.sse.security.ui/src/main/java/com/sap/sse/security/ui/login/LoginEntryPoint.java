package com.sap.sse.security.ui.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint extends AbstractEntryPoint {
    /**
     * The URL parameter name used to pass an application name to this entry point. This name
     * will be put into the login view's header.
     */
    private static final String PARAM_APP = "app";
    private UserManagementServiceAsync userManagementService;
    private UserService userService;
    
    @Override
    public void doOnModuleLoad() {
        final String appName = Window.Location.getParameter(PARAM_APP);
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
        RootPanel rootPanel = RootPanel.get();
        rootPanel.add(new LoginView(userManagementService, userService, appName==null?StringMessages.INSTANCE.signIn():appName));
    }
}
