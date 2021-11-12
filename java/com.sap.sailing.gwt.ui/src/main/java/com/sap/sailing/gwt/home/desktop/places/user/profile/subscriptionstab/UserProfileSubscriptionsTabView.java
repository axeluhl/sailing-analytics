package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView.Presenter;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserProfileSubscriptionsPlace;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/**
 * User profile subscription tab view
 * 
 * @author Tu Tran
 */
public class UserProfileSubscriptionsTabView extends Composite
        implements UserProfileTabView<UserProfileSubscriptionsPlace> {

    private UserProfileSubscriptionsView view;
    private UserProfileSubscriptionsView.Presenter currentPresenter;
    private final DesktopPlacesNavigator homePlacesNavigator;

    public UserProfileSubscriptionsTabView(final DesktopPlacesNavigator homePlacesNavigator) {
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    public Class<UserProfileSubscriptionsPlace> getPlaceClassForActivation() {
        return UserProfileSubscriptionsPlace.class;
    }

    @Override
    public void start(UserProfileSubscriptionsPlace requestedPlace, AcceptsOneWidget contentArea) {
        contentArea.setWidget(view);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        view = new UserProfileSubscriptionsViewImpl();
        currentPresenter = new UserProfileSubscriptionPresenter(homePlacesNavigator, view, presenter);
    }

    @Override
    public void stop() {
    }

    @Override
    public UserProfileSubscriptionsPlace placeToFire() {
        return new UserProfileSubscriptionsPlace();
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
