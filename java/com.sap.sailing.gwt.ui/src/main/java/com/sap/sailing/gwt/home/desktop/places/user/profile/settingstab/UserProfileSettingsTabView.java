package com.sap.sailing.gwt.home.desktop.places.user.profile.settingstab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserProfileSettingsPlace;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfileSettingsTabView extends Composite implements UserProfileTabView<UserProfileSettingsPlace> {

    private UserProfileSettingsView.Presenter currentPresenter;
    private UserProfileView.Presenter currentUserProfileViewPresenter;
    private UserProfileSettingsView view;

    @Override
    public Class<UserProfileSettingsPlace> getPlaceClassForActivation() {
        return UserProfileSettingsPlace.class;
    }

    @Override
    public void start(UserProfileSettingsPlace myPlace, AcceptsOneWidget contentArea) {
        contentArea.setWidget(view);
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        currentPresenter.setAuthenticationContext(authenticationContext);
    }

    @Override
    public void stop() {
    }

    @Override
    public UserProfileSettingsPlace placeToFire() {
        return new UserProfileSettingsPlace();
    }

    @Override
    public void setPresenter(UserProfileView.Presenter currentPresenter) {
        view = new UserProfileSettingsViewImpl();
        this.currentUserProfileViewPresenter = currentPresenter;
        this.currentPresenter = new UserProfileSettingsPresenter(view, currentPresenter);
    }

    @Override
    public UserProfileView.Presenter getPresenter() {
        return currentUserProfileViewPresenter;
    }
}