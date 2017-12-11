package com.sap.sse.security.ui.usermanagement;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.SettingsPanel;
import com.sap.sse.security.ui.client.component.UserManagementPanel;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementEntryPoint extends AbstractSecurityEntryPoint {
    private TabLayoutPanel center;

    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        center = new TabLayoutPanel(2.5, Unit.EM);
        getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
            }
        });
        UserManagementPanel userManagementPanel = new UserManagementPanel(getUserService(), getStringMessages(), this);
        center.add(new ScrollPanel(userManagementPanel), getStringMessages().users());
        final SettingsPanel settingsPanel = new SettingsPanel(getUserManagementService(), getStringMessages());
        center.add(new ScrollPanel(settingsPanel), getStringMessages().settings());
        RootLayoutPanel.get().add(center);
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.loginPanelCss(), getUserService()));
        setTabPanelSize(center, ""+Window.getClientWidth()+"px", ""+Window.getClientHeight()+"px");
    }
}
