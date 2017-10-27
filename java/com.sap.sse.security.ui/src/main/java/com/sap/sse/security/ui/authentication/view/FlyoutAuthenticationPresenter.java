package com.sap.sse.security.ui.authentication.view;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.create.CreateAccountPlace;

/**
 * Default implementation of {@link AuthenticationMenuView.Presenter} and {@link FlyoutAuthenticationView.Presenter} to
 * connect the {@link AuthenticationMenuView menu item} with the {@link FlyoutAuthenticationView flyout}.
 */
public class FlyoutAuthenticationPresenter implements AuthenticationMenuView.Presenter, FlyoutAuthenticationView.Presenter {
    
    private final FlyoutAuthenticationView flyoutAuthenticationView;
    private final WrappedPlaceManagementController authenticationPlaceManagementController;
    private final AuthenticationMenuView authenticationMenuView;

    /**
     * Creates a new {@link FlyoutAuthenticationPresenter} instance with the given parameters.
     * 
     * @param flyoutAuthenticationView
     *            the {@link FlyoutAuthenticationView} to toggle via menu item
     * @param authenticationMenuView
     *            the {@link AuthenticationMenuView} interacting with the flyout
     * @param authenticationPlaceManagementController
     *            the {@link WrappedPlaceManagementController} which is started if the flyout is shown
     * @param eventBus
     *            the {@link EventBus} of the application which uses the wrapped framework
     * @param initialAuthentication
     *            the initial {@link AuthenticationContext}, which is needed to provide initial state, cause the
     *            eventBus informs the presenter only in case of state changes
     */
    public FlyoutAuthenticationPresenter(final FlyoutAuthenticationView flyoutAuthenticationView,
            final AuthenticationMenuView authenticationMenuView,
            AuthenticationPlaceManagementController authenticationPlaceManagementController,
            EventBus eventBus, AuthenticationContext initialAuthentication) {
        this.flyoutAuthenticationView = flyoutAuthenticationView;
        this.authenticationMenuView = authenticationMenuView;
        this.authenticationPlaceManagementController = authenticationPlaceManagementController;
        
        flyoutAuthenticationView.setPresenter(this);
        
        authenticationMenuView.setPresenter(this);
        flyoutAuthenticationView.setAutoHidePartner(authenticationMenuView);
        
        eventBus.addHandler(AuthenticationRequestEvent.TYPE, event -> {
            flyoutAuthenticationView.show();
            authenticationPlaceManagementController.goTo(event.getRequestedPlace());
        });
        
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                authenticationMenuView.setAuthenticated(event.getCtx().isLoggedIn());
            }
        });
        authenticationMenuView.setAuthenticated(initialAuthentication.isLoggedIn());
    }

    public void showRegister() {
        if (!flyoutAuthenticationView.isShowing()) {
            flyoutAuthenticationView.show();
            authenticationPlaceManagementController.start();
            authenticationPlaceManagementController.goTo(new CreateAccountPlace());
        }
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
