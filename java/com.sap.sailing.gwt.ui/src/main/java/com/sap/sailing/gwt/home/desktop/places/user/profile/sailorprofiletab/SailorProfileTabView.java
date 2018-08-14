package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.UUID;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetails;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class SailorProfileTabView extends Composite implements UserProfileTabView<SailorProfilePlace> {

    private SailingProfileOverviewPresenter currentPresenter;
    private SailorProfileView view;
    private final FlagImageResolver flagImageResolver;

    UserProfileView.Presenter ownPresenter;

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
        UUID uuid = myPlace.getSailorProfileUuid();
        if (uuid != null) {
            final SailorProfileDetails sailorView = new SailorProfileDetails();
            currentPresenter = new SailorProfileOverviewImplPresenter(sailorView, ownPresenter, flagImageResolver);
            contentArea.setWidget(sailorView);
            view = sailorView;
            currentPresenter.getSharedSailorProfilePresenter().getDataProvider().setView(sailorView.getEditView());
            if (uuid == SailorProfileDTO.SAILOR_PROFILE_KEY_NEW) {
                SailorProfileDTO newEntry = new SailorProfileDTO(UUID.randomUUID(), "New Sailor Profile");
                currentPresenter.getSharedSailorProfilePresenter().getDataProvider().createNewEntry(newEntry);
            } else {
                currentPresenter.getSharedSailorProfilePresenter().getDataProvider().loadSailorProfile(uuid);
            }
        } else {
            view = new SailorProfileOverviewImpl();
            this.currentPresenter = new SailorProfileOverviewImplPresenter(view, ownPresenter, flagImageResolver);
            contentArea.setWidget(view);
        }
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        currentPresenter.setAuthenticationContext(authenticationContext);
        view.getAuthenticationContext().setAuthenticationContext(authenticationContext);
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
