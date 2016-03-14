package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsPresenter;

public class UserProfileDetailsPresenter implements UserProfileDetailsView.Presenter {

    private final UserProfileDetailsView view;
    private final UserDetailsPresenter userDetailsPresenter;

    public UserProfileDetailsPresenter(UserProfileDetailsView view, UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        
        view.setPresenter(this);

        userDetailsPresenter = new UserDetailsPresenter(view.getUserDetailsView(),
                userProfilePresenter.getAuthenticationManager(), userProfilePresenter.getUserManagementService(),
                userProfilePresenter.getMailVerifiedUrl());
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        userDetailsPresenter.setAuthenticationContext(authenticationContext);
    }
}
