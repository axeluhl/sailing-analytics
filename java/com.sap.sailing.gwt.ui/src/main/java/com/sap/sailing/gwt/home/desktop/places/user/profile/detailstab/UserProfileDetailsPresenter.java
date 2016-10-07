package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsPresenter;

public class UserProfileDetailsPresenter implements UserProfileDetailsView.Presenter {

    private final UserProfileDetailsView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final UserDetailsPresenter userDetailsPresenter;

    public UserProfileDetailsPresenter(UserProfileDetailsView view, UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        view.setPresenter(this);
        this.userDetailsPresenter = new UserDetailsPresenter(view.getUserDetailsView(),
                userProfilePresenter.getClientFactory().getAuthenticationManager(),
                userProfilePresenter.getClientFactory().getUserManagementService(),
                userProfilePresenter.getMailVerifiedUrl());
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        userDetailsPresenter.setAuthenticationContext(authenticationContext);
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
}
