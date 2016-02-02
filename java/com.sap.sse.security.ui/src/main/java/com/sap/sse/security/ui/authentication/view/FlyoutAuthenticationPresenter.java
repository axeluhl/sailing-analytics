package com.sap.sse.security.ui.authentication.view;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;

public class FlyoutAuthenticationPresenter implements AuthenticationMenuView.Presenter, FlyoutAuthenticationView.Presenter {
    
    private final FlyoutAuthenticationView flyoutAuthenticationView;
    private final WrappedPlaceManagementController authenticationPlaceManagementController;
    private final AuthenticationMenuView authenticationMenuView;

    public FlyoutAuthenticationPresenter(final FlyoutAuthenticationView flyoutAuthenticationView,
            final AuthenticationMenuView authenticationMenuView,
            WrappedPlaceManagementController authenticationPlaceManagementController,
            EventBus eventBus) {
        this.flyoutAuthenticationView = flyoutAuthenticationView;
        this.authenticationMenuView = authenticationMenuView;
        this.authenticationPlaceManagementController = authenticationPlaceManagementController;
        
        flyoutAuthenticationView.setPresenter(this);
        
        authenticationMenuView.setPresenter(this);
        flyoutAuthenticationView.setAutoHidePartner(authenticationMenuView);
        
        eventBus.addHandler(AuthenticationRequestEvent.TYPE, new AuthenticationRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(AuthenticationRequestEvent event) {
                toggleFlyout();
            }
        });
        
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                authenticationMenuView.setAuthenticated(event.getCtx().isLoggedIn());
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

    @Override
    public void onVisibilityChanged(boolean isShowing) {
        authenticationMenuView.setOpen(isShowing);
    }

}
