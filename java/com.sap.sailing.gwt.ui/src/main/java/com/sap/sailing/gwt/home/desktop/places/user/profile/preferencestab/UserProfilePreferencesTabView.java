package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserProfilePreferencesPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesTabView extends Composite implements UserProfileTabView<UserProfilePreferencesPlace> {

    private UserProfilePreferencesView.Presenter currentPresenter;
    private UserProfilePreferencesView view;
    private final FlagImageResolver flagImageResolver;
    
    public UserProfilePreferencesTabView(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public Class<UserProfilePreferencesPlace> getPlaceClassForActivation() {
        return UserProfilePreferencesPlace.class;
    }

    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(UserProfilePreferencesPlace myPlace, AcceptsOneWidget contentArea) {
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
    public UserProfilePreferencesPlace placeToFire() {
        return new UserProfilePreferencesPlace();
    }

    @Override
    public void setPresenter(UserProfileView.Presenter currentPresenter) {
        view = new UserProfilePreferencesViewImpl(flagImageResolver);
        this.currentPresenter = new UserProfilePreferencesPresenter(view, currentPresenter);
    }
}