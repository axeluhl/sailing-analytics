package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesPresenter implements UserProfilePreferencesView.Presenter {

    private final UserProfilePreferencesView view;
    private final UserProfileView.Presenter userProfilePresenter;
//    private final UserDetailsPresenter userDetailsPresenter;

    public UserProfilePreferencesPresenter(UserProfilePreferencesView view, UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        view.setPresenter(this);
//        this.userDetailsPresenter = new UserDetailsPresenter(view.getUserDetailsView(),
//                userProfilePresenter.getAuthenticationManager(), userProfilePresenter.getUserManagementService(),
//                userProfilePresenter.getMailVerifiedUrl());
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
//        userDetailsPresenter.setAuthenticationContext(authenticationContext);
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
}
