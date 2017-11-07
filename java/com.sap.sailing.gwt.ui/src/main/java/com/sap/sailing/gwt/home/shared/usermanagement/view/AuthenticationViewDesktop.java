package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.view.AbstractFlyoutAuthenticationView;

public class AuthenticationViewDesktop extends AbstractFlyoutAuthenticationView {

    public AuthenticationViewDesktop() {
        super(SharedResources.INSTANCE);
        popupPanel.addStyleName(UserManagementResources.INSTANCE.css().flyover_position_grid());
    }
    
    public void show() {
        getPresenter().onVisibilityChanged(true);
        popupPanel.show();
        popupPanel.getElement().getStyle().clearLeft();
        popupPanel.getElement().getStyle().clearTop();
    }
}
