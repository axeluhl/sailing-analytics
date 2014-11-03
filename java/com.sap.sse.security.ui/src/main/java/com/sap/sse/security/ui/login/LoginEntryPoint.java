package com.sap.sse.security.ui.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint extends AbstractEntryPoint {
    private UserManagementServiceAsync userManagementService;
    private UserService userService;
    
    @Override
    public void doOnModuleLoad() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);

        dockPanel.add(new LoginView(userManagementService, userService));
    }
}
