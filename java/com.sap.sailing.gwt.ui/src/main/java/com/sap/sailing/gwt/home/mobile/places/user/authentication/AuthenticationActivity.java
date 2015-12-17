package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementView;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementViewMobile;

public class AuthenticationActivity extends AbstractActivity {
    private final MobileApplicationClientFactory clientFactory;
    private final UserManagementView userManagementView = new UserManagementViewMobile();

    public AuthenticationActivity(AuthenticationPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(userManagementView);
        WrappedPlaceManagementController userManagementController = 
                new UserManagementPlaceManagementController<MobileApplicationClientFactory>(clientFactory, 
                        clientFactory.getNavigator().getCreateConfirmationNavigation(), clientFactory
                        .getNavigator().getUserProfileNavigation(), userManagementView, eventBus);
        userManagementController.start();
    }
    
}
