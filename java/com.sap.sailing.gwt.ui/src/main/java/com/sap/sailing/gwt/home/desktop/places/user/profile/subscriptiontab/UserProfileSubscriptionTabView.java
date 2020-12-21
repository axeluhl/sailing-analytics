package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView.Presenter;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserProfileSubscriptionPlace;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/**
 * User profile subscription tab view
 * 
 * @author Tu Tran
 */
public class UserProfileSubscriptionTabView extends Composite
        implements UserProfileTabView<UserProfileSubscriptionPlace> {

    private UserProfileSubscriptionView view;
    private UserProfileSubscriptionView.Presenter currentPresenter;

    @Override
    public Class<UserProfileSubscriptionPlace> getPlaceClassForActivation() {
        return UserProfileSubscriptionPlace.class;
    }

    @Override
    public void start(UserProfileSubscriptionPlace requestedPlace, AcceptsOneWidget contentArea) {
        contentArea.setWidget(view);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        view = new UserProfileSubscriptionViewImpl();
        currentPresenter = new UserProfileSubscriptionPresenter(view, presenter);
    }

    @Override
    public void stop() {
    }

    @Override
    public UserProfileSubscriptionPlace placeToFire() {
        return new UserProfileSubscriptionPlace();
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        currentPresenter.setAuthenticationContext(authenticationContext);
    }

    @Override
    public TabView.State getState() {
        return ExperimentalFeatures.SHOW_SUBSCRIPTION_IN_USER_PROFILE ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }
}
