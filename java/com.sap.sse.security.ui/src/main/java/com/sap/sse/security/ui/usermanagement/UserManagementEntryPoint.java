package com.sap.sse.security.ui.usermanagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.SettingsPanel;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;
import com.sap.sse.security.ui.loginpanel.LoginPanel;

public class UserManagementEntryPoint extends AbstractSecurityEntryPoint {
    private final CellTableWithCheckboxResources tableResources = GWT.create(CellTableWithCheckboxResources.class);

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
        UserManagementPanel<CellTableWithCheckboxResources> userManagementPanel = new UserManagementPanel<>(getUserService(), getStringMessages(), this, tableResources);
        center.add(new ScrollPanel(userManagementPanel), getStringMessages().users());
        final SettingsPanel settingsPanel = new SettingsPanel(getUserManagementService(), getStringMessages());
        center.add(new ScrollPanel(settingsPanel), getStringMessages().settings());
        RootLayoutPanel.get().add(center);
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.loginPanelCss(), getUserService()));
        setTabPanelSize(center, ""+Window.getClientWidth()+"px", ""+Window.getClientHeight()+"px");
    }
}
