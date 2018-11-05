package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.UUID;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetails;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.view.SailorProfileView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/** main entry point for sailor profiles overview on desktop */
public class SailorProfileTabView extends Composite implements UserProfileTabView<SailorProfilePlace> {

    private SailingProfileOverviewPresenter currentPresenter;
    private final FlagImageResolver flagImageResolver;

    private UserProfileView.Presenter ownPresenter;
    private AcceptsOneWidget contentArea;
    private SailorProfilePlace place;

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
        this.place = myPlace;
        this.contentArea = contentArea;
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        UUID uuid = place.getSailorProfileUuid();
        SailorProfileView view;
        if (uuid != null || place.isCreateNew()) {
            view = new SailorProfileDetails();
        } else {
            view = new SailorProfileOverviewImpl();
        }

        this.currentPresenter = new SailorProfileOverviewImplPresenter(view,
                new ClientFactoryAdapter(this.ownPresenter), flagImageResolver);
        currentPresenter.setAuthenticationContext(authenticationContext);
        contentArea.setWidget(view);

        if (authenticationContext.isLoggedIn()) {
            if (uuid != null || place.isCreateNew()) {
                if (place.isCreateNew()) {
                    currentPresenter.getSharedSailorProfilePresenter().getDataProvider()
                            .createNewEntry(UUID.randomUUID(), StringMessages.INSTANCE.newSailorProfileName());
                } else {
                    currentPresenter.getSharedSailorProfilePresenter().getDataProvider()
                            .loadSailorProfile(place.getSailorProfileUuid());
                }
            }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public SailorProfilePlace placeToFire() {
        return new SailorProfilePlace(null);
    }

    @Override
    public void setPresenter(UserProfileView.Presenter ownPresenter) {
        this.ownPresenter = ownPresenter;
    }
}
