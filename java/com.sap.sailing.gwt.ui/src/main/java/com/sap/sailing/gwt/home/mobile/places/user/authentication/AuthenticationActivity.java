package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationClientFactoryImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementCallbackImpl;
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
        WrappedPlaceManagementController userManagementController = new UserManagementPlaceManagementController(
                new AuthenticationClientFactoryImpl(SharedResources.INSTANCE),
                clientFactory, new UserManagementCallbackImpl(clientFactory.getNavigator()
                        .getMailVerifiedConfirmationNavigation(), clientFactory.getNavigator()
                        .getPasswordResetNavigation(), clientFactory.getNavigator().getUserProfileNavigation(),
                        new SignInSuccessfulNavigationMobile()), userManagementView, eventBus);
        userManagementController.start();
    }
    
    private class SignInSuccessfulNavigationMobile implements Runnable {
        @Override
        public void run() {
            History.back();
        }
    }
    
}
