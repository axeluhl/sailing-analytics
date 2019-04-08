package com.sap.sse.security.ui.authentication;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoActivity;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoPlace;
import com.sap.sse.security.ui.authentication.create.CreateAccountActivity;
import com.sap.sse.security.ui.authentication.create.CreateAccountPlace;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoActivity;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoPlace;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryActivity;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryPlace;
import com.sap.sse.security.ui.authentication.signin.SignInActivity;
import com.sap.sse.security.ui.authentication.signin.SignInPlace;
import com.sap.sse.security.ui.authentication.view.AuthenticationView;

/**
 * Controller class for wrapped authentication management, which extends and configures the
 * {@link WrappedPlaceManagementController} It also registers an {@link AuthenticationContextEvent.Handler} at the given
 * global {@link EventBus} to pass the respective events to the wrapped authentication management.
 */
public class AuthenticationPlaceManagementController extends WrappedPlaceManagementController {
    
    private final AuthenticationClientFactory clientFactory;

    /**
     * Creates a new {@link AuthenticationPlaceManagementController} instance with the given parameters.
     * 
     * @param clientFactory the {@link AuthenticationClientFactory} to use
     * @param callback the {@link AuthenticationCallback} to use
     * @param authenticationView the {@link AuthenticationView} to use
     * @param globalEventBus the {@link EventBus} of the application which uses the wrapped framework
     */
    public AuthenticationPlaceManagementController(AuthenticationClientFactory clientFactory,
            AuthenticationCallback callback, AuthenticationView authenticationView, EventBus globalEventBus) {
        super(new Configuration(clientFactory, callback, authenticationView));
        this.clientFactory = clientFactory;
        globalEventBus.addHandler(AuthenticationContextEvent.TYPE, this::fireEvent);
    }

    /**
     * Tells the wrapped framework to go to the {@link Place} represented by the given {@link AuthenticationPlaces}
     * instance is no user is logged in. Otherwise the wrapped framework will go to the {@link LoggedInUserInfoPlace
     * user info} page.
     * 
     * @param authPlace
     *            {@link AuthenticationPlaces} instance representing a {@link Place} instance to go to
     * 
     * @see #goTo(Place)
     */
    public void goTo(AuthenticationPlaces authPlace) {
        final boolean isLoggedIn = clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn();
        super.goTo(isLoggedIn ? new LoggedInUserInfoPlace() : authPlace.getPlace());
    }

    private static class Configuration implements PlaceManagementConfiguration {
        private final AuthenticationClientFactory clientFactory;
        private final AuthenticationCallback callback;
        private final AuthenticationView authenticationView;
        private PlaceController placeController;

        private Configuration(AuthenticationClientFactory clientFactory, AuthenticationCallback callback,
                AuthenticationView authenticationView) {
            this.clientFactory = clientFactory;
            this.callback = callback;
            this.authenticationView = authenticationView;
        }
        
        @Override
        public void setPlaceController(PlaceController placeController) {
            this.placeController = placeController;
        }
        
        @Override
        public AcceptsOneWidget getDisplay() {
            return authenticationView;
        }
        
        @Override
        public Place getStartPlace() {
            return isLoggedIn() ? new LoggedInUserInfoPlace() : new SignInPlace();
        }

        @Override
        public Activity getActivity(Place requestedPlace) {
            final Place placeToUse = getPlaceToUse(requestedPlace);
            this.updateViewHeading(placeToUse);
            
            if (placeToUse instanceof SignInPlace) {
                return new SignInActivity(clientFactory, callback, placeController);
            } else if (placeToUse instanceof CreateAccountPlace) {
                return new CreateAccountActivity(clientFactory, placeController);
            } else if (placeToUse instanceof PasswordRecoveryPlace) {
                return new PasswordRecoveryActivity(clientFactory, placeController);
            } else if (placeToUse instanceof LoggedInUserInfoPlace) {
                return new LoggedInUserInfoActivity(clientFactory, callback, placeController);
            } else if (placeToUse instanceof ConfirmationInfoPlace) {
                return new ConfirmationInfoActivity(clientFactory, (ConfirmationInfoPlace) placeToUse);
            }
            
            return getActivity(new SignInPlace());
        }
        
        private boolean isLoggedIn() {
            return clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn();
        }
        
        private Place getPlaceToUse(Place requestedPlace) {
            boolean requiresLogin = requestedPlace instanceof RequiresLoggedInUser;
            return requiresLogin && !isLoggedIn() ? getStartPlace() : requestedPlace;
        }
        
        private void updateViewHeading(Place placeToUse) {
            authenticationView.setHeading(placeToUse instanceof AbstractAuthenticationPlace
                    ? ((AbstractAuthenticationPlace) placeToUse).getHeaderText() : "");
        }

    }

}
