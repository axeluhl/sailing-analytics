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
import com.sap.sse.security.ui.authentication.view.UserManagementView;

public class AuthenticationPlaceManagementController extends WrappedPlaceManagementController {
    
    public AuthenticationPlaceManagementController(AuthenticationClientFactory clientFactory, AuthenticationCallback callback,
            UserManagementView userManagementView, EventBus globalEventBus) {
        super(new Configuration(clientFactory, callback, userManagementView));
        globalEventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                AuthenticationPlaceManagementController.this.fireEvent(event);
            }
        });
    }
    
    private static class Configuration implements PlaceManagementConfiguration {
        private final AuthenticationClientFactory clientFactory;
        private final AuthenticationCallback callback;
        private final UserManagementView userManagementView;
        private PlaceController placeController;

        public Configuration(AuthenticationClientFactory clientFactory, AuthenticationCallback callback,
                UserManagementView userManagementView) {
            this.clientFactory = clientFactory;
            this.callback = callback;
            this.userManagementView = userManagementView;
        }
        
        @Override
        public void setPlaceController(PlaceController placeController) {
            this.placeController = placeController;
        }
        
        @Override
        public AcceptsOneWidget getDisplay() {
            return userManagementView;
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
                return new SignInActivity(clientFactory.createSignInView(), clientFactory, callback, placeController);
            } else if (placeToUse instanceof CreateAccountPlace) {
                return new CreateAccountActivity(clientFactory.createCreateAccountView(), clientFactory, callback,
                        placeController);
            } else if (placeToUse instanceof PasswordRecoveryPlace) {
                return new PasswordRecoveryActivity(clientFactory.createPasswordRecoveryView(), clientFactory,
                        callback, placeController);
            } else if (placeToUse instanceof LoggedInUserInfoPlace) {
                return new LoggedInUserInfoActivity(clientFactory.createLoggedInUserInfoView(), clientFactory,
                        callback, placeController);
            } else if (placeToUse instanceof ConfirmationInfoPlace) {
                return new ConfirmationInfoActivity((ConfirmationInfoPlace) placeToUse,
                        clientFactory.createConfirmationInfoView());
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
            userManagementView.setHeading(placeToUse instanceof AbstractAuthenticationPlace
                    ? ((AbstractAuthenticationPlace) placeToUse).getHeaderText() : "");
        }

    }

}
