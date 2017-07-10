package com.sap.sse.security.ui.usermanagement;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.SettingsPanel;
import com.sap.sse.security.ui.client.component.UserManagementPanel;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementEntryPoint extends AbstractSecurityEntryPoint {
    private TabLayoutPanel center;
    
    private UserDTO user;
    
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        center = new TabLayoutPanel(2.5, Unit.EM);
        getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                if (!hasRequiredRole(user) && hasRequiredRole(UserManagementEntryPoint.this.user)) {
                    Window.Location.reload(); // user signed out or lost required role; reload to take the user to the log-in screen
                }
                UserManagementEntryPoint.this.user = user;
            }
        });
        UserManagementPanel userManagementPanel = new UserManagementPanel(getUserService(), getStringMessages(),
                /* permissionsForRoleProvider is null in this generic entry point */ null);
        center.add(new ScrollPanel(userManagementPanel), getStringMessages().users());
        final SettingsPanel settingsPanel = new SettingsPanel(getUserManagementService(), getStringMessages());
        center.add(new ScrollPanel(settingsPanel), getStringMessages().settings());
        RootLayoutPanel.get().add(center);
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.loginPanelCss(), getUserService()));
        setTabPanelSize(center, ""+Window.getClientWidth()+"px", ""+Window.getClientHeight()+"px");
    }

    private boolean hasRequiredRole(UserDTO user) {
        return user != null && user.hasRole(DefaultRoles.ADMIN.getRolename());
    }

}
