package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlaceManagementController;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationActivity;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.usermanagement.app.ClientFactoryWithUserManagementContext;
import com.sap.sailing.gwt.home.shared.usermanagement.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementView;
import com.sap.sse.gwt.client.mvp.ClientFactory;

public class UserManagementPlaceManagementController
        <CF extends ClientFactory & ClientFactoryWithUserManagementContext & ClientFactoryWithUserManagementService & ConfirmationClientFactory>
        extends WrappedPlaceManagementController {

    public UserManagementPlaceManagementController(CF clientFactory,
            PlaceNavigation<ConfirmationPlace> createConfirmationNavigation,
            PlaceNavigation<PasswordResetPlace> passwordResetPlaceNav,
            PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation,
            UserManagementView userManagementView, EventBus globalEventBus) {
        super(new Configuration<CF>(clientFactory, createConfirmationNavigation, passwordResetPlaceNav, userProfileNavigation, userManagementView));
        globalEventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                UserManagementPlaceManagementController.this.fireEvent(event);
            }
        });
    }
    
    private static class Configuration
            <CF extends ClientFactory & ClientFactoryWithUserManagementContext & ClientFactoryWithUserManagementService & ConfirmationClientFactory>
            implements PlaceManagementConfiguration {
        private final CF clientFactory;
        private final PlaceNavigation<ConfirmationPlace> createConfirmationNavigation;
        private final PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation;
        private final UserManagementView userManagementView;
        private PlaceController placeController;
        private final PlaceNavigation<PasswordResetPlace> passwordResetPlaceNav;

        public Configuration(CF clientFactory, PlaceNavigation<ConfirmationPlace> createConfirmationNavigation,
                PlaceNavigation<PasswordResetPlace> passwordResetPlaceNav,
                PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation,
                UserManagementView userManagementView) {
            this.clientFactory = clientFactory;
            this.createConfirmationNavigation = createConfirmationNavigation;
            this.passwordResetPlaceNav = passwordResetPlaceNav;
            this.userProfileNavigation = userProfileNavigation;
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
                return new SignInActivity((SignInPlace) placeToUse, clientFactory, placeController);
            } else if (placeToUse instanceof CreateAccountPlace) {
                return new CreateAccountActivity((CreateAccountPlace) placeToUse, clientFactory,
                        createConfirmationNavigation, placeController);
            } else if (placeToUse instanceof PasswordRecoveryPlace) {
                return new PasswordRecoveryActivity<CF>((PasswordRecoveryPlace) placeToUse, clientFactory, passwordResetPlaceNav, placeController);
            } else if (placeToUse instanceof LoggedInUserInfoPlace) {
                return new LoggedInUserInfoActivity<CF>((LoggedInUserInfoPlace) placeToUse, clientFactory,
                        userProfileNavigation, placeController);
            } else if (placeToUse instanceof ConfirmationPlace) {
                return new ConfirmationActivity((ConfirmationPlace) placeToUse, clientFactory);
            }
            
            return getActivity(new SignInPlace());
        }
        
        private boolean isLoggedIn() {
            return clientFactory.getUserManagementContext().isLoggedIn();
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
    
    public static class SignInSuccessfulEvent extends GwtEvent<SignInSuccessfulEvent.Handler> {
        
        public static final Type<SignInSuccessfulEvent.Handler> TYPE = new Type<>();
        
        public interface Handler extends EventHandler {
            public void onSignInSuccessful(SignInSuccessfulEvent event);
        }

        @Override
        public Type<Handler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(Handler handler) {
            handler.onSignInSuccessful(this);
        }

    }
    

}
