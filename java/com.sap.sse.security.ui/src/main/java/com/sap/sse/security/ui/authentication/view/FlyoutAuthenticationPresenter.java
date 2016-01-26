package com.sap.sse.security.ui.authentication.view;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;

public class FlyoutAuthenticationPresenter implements AuthenticationMenuView.Presenter {
    
    private final FlyoutAuthenticationView flyoutAuthenticationView;
    private final WrappedPlaceManagementController authenticationPlaceManagementController;

    public FlyoutAuthenticationPresenter(final FlyoutAuthenticationView flyoutAuthenticationView,
            AuthenticationMenuView authenticationMenuView,
            WrappedPlaceManagementController authenticationPlaceManagementController,
            EventBus eventBus) {
        this.flyoutAuthenticationView = flyoutAuthenticationView;
        this.authenticationPlaceManagementController = authenticationPlaceManagementController;
        
        authenticationMenuView.setPresenter(this);
        flyoutAuthenticationView.setAutoHidePartner(authenticationMenuView);
        
        eventBus.addHandler(AuthenticationRequestEvent.TYPE, new AuthenticationRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(AuthenticationRequestEvent event) {
                toggleFlyout();
            }
        });
    }

    @Override
    public void toggleFlyout() {
        if (flyoutAuthenticationView.isShowing()) {
            flyoutAuthenticationView.hide();
        } else {
            flyoutAuthenticationView.show();
            authenticationPlaceManagementController.start();
        }
    }

}
