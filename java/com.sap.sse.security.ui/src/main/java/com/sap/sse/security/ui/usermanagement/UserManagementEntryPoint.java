package com.sap.sse.security.ui.usermanagement;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.SettingsPanel;
import com.sap.sse.security.ui.client.component.UserManagementPanel;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserManagementEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    private final StringMessages stringMessages = GWT.create(StringMessages.class);
    
    private TabLayoutPanel center;
    
    private UserDTO user;
    
    @Override
    public void onModuleLoad() {
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                RemoteServiceMappingConstants.userManagementServiceRemotePath);
        UserService userService = new UserService(userManagementService);
        center = new TabLayoutPanel(2.5, Unit.EM);
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                if (user == null && UserManagementEntryPoint.this.user != null) {
                    Window.Location.reload();
                }
                UserManagementEntryPoint.this.user = user;
            }
        });
        UserManagementPanel userManagementPanel = new UserManagementPanel(userService, stringMessages);
        center.add(new ScrollPanel(userManagementPanel), stringMessages.users());
        final SettingsPanel settingsPanel = new SettingsPanel(userManagementService, stringMessages);
        center.add(new ScrollPanel(settingsPanel), stringMessages.settings());
        RootLayoutPanel.get().add(center);
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.css(), userService));
        setTabPanelSize(center, ""+Window.getClientWidth()+"px", ""+Window.getClientHeight()+"px");
    }
    
    /**
     * Sets the size of the tab panel when the tab panel is attached to the DOM
     */
    public static void setTabPanelSize(final TabLayoutPanel advancedTabPanel, final String width, final String height) {
        advancedTabPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("width", width);
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("height", height);
            }
        });
    }

}
