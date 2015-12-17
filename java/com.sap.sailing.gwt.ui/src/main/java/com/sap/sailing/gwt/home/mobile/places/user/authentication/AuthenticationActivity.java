package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlacesManagementController;
import com.sap.sailing.gwt.home.shared.framework.WrappedPlacesManagementController.StartPlaceActivityMapper;
import com.sap.sailing.gwt.home.shared.usermanagement.AbstractUserManagementPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.RequiresLoggedInUser;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInActivity;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementView;
import com.sap.sailing.gwt.home.shared.usermanagement.view.UserManagementViewMobile;

public class AuthenticationActivity extends AbstractActivity {
    private final MobileApplicationClientFactory clientFactory;
    private final UserManagementView userManagementView = new UserManagementViewMobile();
    private final WrappedPlacesManagementController userManagementController = new WrappedPlacesManagementController(
            new MobileUserManagementStartPlaceActivityMapper(), userManagementView);

    public AuthenticationActivity(AuthenticationPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(userManagementView);
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                userManagementController.fireEvent(event);
            }
        });
        userManagementController.start();
    }
    
    private class MobileUserManagementStartPlaceActivityMapper implements StartPlaceActivityMapper {

        private PlaceController placeController;

        @Override
        public Activity getActivity(final Place requestedPlace) {
            final Place placeToUse;
            if (requestedPlace instanceof RequiresLoggedInUser && !clientFactory.getUserManagementContext().isLoggedIn()) {
                placeToUse = getStartPlace();
            } else {
                placeToUse = requestedPlace;
            }
            
            userManagementView.setHeading(placeToUse instanceof AbstractUserManagementPlace
                            ? ((AbstractUserManagementPlace) placeToUse).getLocationTitle() : "");
            
            if (placeToUse instanceof SignInPlace) {
                return new SignInActivity((SignInPlace) placeToUse, clientFactory, placeController);
            } else if (placeToUse instanceof CreateAccountPlace) {
                return new CreateAccountActivity((CreateAccountPlace) placeToUse, clientFactory, 
                        clientFactory.getNavigator().getCreateConfirmationNavigation(), placeController);
            } else if (placeToUse instanceof PasswordRecoveryPlace) {
                return new PasswordRecoveryActivity<MobileApplicationClientFactory>(
                        (PasswordRecoveryPlace) placeToUse, clientFactory, placeController);
            } else if (placeToUse instanceof LoggedInUserInfoPlace) {
                History.back();
                return null;
            }
            
            return getActivity(new SignInPlace(new LoggedInUserInfoPlace()));
        }
        
        @Override
        public Place getStartPlace() {
            if (clientFactory.getUserManagementContext().isLoggedIn()) {
                return new LoggedInUserInfoPlace();
            } else {
                return new SignInPlace(new LoggedInUserInfoPlace());
            }
        }

        @Override
        public void setPlaceController(PlaceController placeController) {
            this.placeController = placeController;
        }
    }


}
