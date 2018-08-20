package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationCallbackImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.view.AuthenticationViewMobile;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactoryImpl;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.view.AuthenticationView;

public class AuthenticationActivity extends AbstractActivity {
    private final MobileApplicationClientFactory clientFactory;
    private final AuthenticationView userManagementView = new AuthenticationViewMobile();
    private final AuthenticationPlace place;

    public AuthenticationActivity(AuthenticationPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(userManagementView);
        final AuthenticationPlaceManagementController userManagementController = new AuthenticationPlaceManagementController(
                new AuthenticationClientFactoryImpl(clientFactory.getAuthenticationManager(), SharedResources.INSTANCE),
                new AuthenticationCallbackImpl(clientFactory.getNavigator().getUserProfileNavigation(),
                        () -> History.back()),
                userManagementView, eventBus);
        userManagementController.goTo(place.getRequestedPlace());
    }

}
