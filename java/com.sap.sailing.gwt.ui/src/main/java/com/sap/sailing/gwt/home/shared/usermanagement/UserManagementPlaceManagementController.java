package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlaceManagementController;
import com.sap.sailing.gwt.home.shared.places.user.UserManagementClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationActivity;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountView;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryView;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementView;

public class UserManagementPlaceManagementController extends WrappedPlaceManagementController {
    
    public interface Callback extends CreateAccountView.Presenter.Callback,
            PasswordRecoveryView.Presenter.Callback, LoggedInUserInfoView.Presenter.Callback {
        
        void handleSignInSuccess();
    }
    
    public UserManagementPlaceManagementController(AuthenticationClientFactory clientFactory,
            UserManagementClientFactory userManagementClientFactory, Callback callback,
            UserManagementView userManagementView, EventBus globalEventBus) {
        super(new Configuration(clientFactory, userManagementClientFactory, callback, userManagementView));
        globalEventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                UserManagementPlaceManagementController.this.fireEvent(event);
            }
        });
    }
    
    private static class Configuration implements PlaceManagementConfiguration {
        private final AuthenticationClientFactory clientFactory;
        private final UserManagementClientFactory userManagementClientFactory;
        private final Callback callback;
        private final UserManagementView userManagementView;
        private PlaceController placeController;

        public Configuration(AuthenticationClientFactory clientFactory,
                UserManagementClientFactory userManagementClientFactory, Callback callback,
                UserManagementView userManagementView) {
            this.clientFactory = clientFactory;
            this.userManagementClientFactory = userManagementClientFactory;
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
            } else if (placeToUse instanceof ConfirmationPlace) {
                return new ConfirmationActivity((ConfirmationPlace) placeToUse,
                        clientFactory.createConfirmationView(), userManagementClientFactory);
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
            userManagementView.setHeading(placeToUse instanceof AbstractUserManagementPlace
                    ? ((AbstractUserManagementPlace) placeToUse).getLocationTitle() : "");
        }

    }

}
