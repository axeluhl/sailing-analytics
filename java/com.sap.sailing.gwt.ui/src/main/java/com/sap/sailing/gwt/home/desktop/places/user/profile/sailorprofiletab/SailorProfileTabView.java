package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileOverviewWrapper;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class SailorProfileTabView extends Composite implements UserProfileTabView<SailorProfilePlace> {

    private SailorProfileOverviewWrapper.Presenter currentPresenter;
    private SailorProfileOverview view;
    private final FlagImageResolver flagImageResolver;

    public SailorProfileTabView(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public Class<SailorProfilePlace> getPlaceClassForActivation() {
        return SailorProfilePlace.class;
    }

    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(SailorProfilePlace myPlace, AcceptsOneWidget contentArea) {
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
    public SailorProfilePlace placeToFire() {
        return new SailorProfilePlace();
    }

    @Override
    public void setPresenter(UserProfileView.Presenter currentPresenter) {
        view = new SailorProfileOverviewImpl(flagImageResolver);
        this.currentPresenter = new SailorProfilePresenterOverviewImpl(view, null, currentPresenter);
    }
}
