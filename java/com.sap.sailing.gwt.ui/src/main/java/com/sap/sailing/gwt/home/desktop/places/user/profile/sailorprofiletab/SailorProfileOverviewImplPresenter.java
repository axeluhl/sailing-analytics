package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfilePresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView.Presenter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class SailorProfileOverviewImplPresenter implements SailingProfileOverviewPresenter {

    private final SailorProfileView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final EditSailorProfileView.Presenter sharedSailorProfilePresenter;
    private final FlagImageResolver flagImageResolver;

    public SailorProfileOverviewImplPresenter(final SailorProfileView view,
            final UserProfileView.Presenter userProfilePresenter, final FlagImageResolver flagImageResolver) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        this.sharedSailorProfilePresenter = new EditSailorProfilePresenter<UserProfileClientFactory>(
                userProfilePresenter.getClientFactory());
        this.flagImageResolver = flagImageResolver;
        view.setPresenter(this);
    }

    @Override
    public ClientFactory getClientFactory() {
        return userProfilePresenter.getClientFactory();
    }

    @Override
    public Presenter getSharedSailorProfilePresenter() {
        return sharedSailorProfilePresenter;
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        if (authenticationContext.isLoggedIn() && view instanceof SailorProfileOverview) {
            sharedSailorProfilePresenter.getDataProvider().loadSailorProfiles(new AsyncCallback<SailorProfilesDTO>() {

                @Override
                public void onSuccess(SailorProfilesDTO result) {
                    ((SailorProfileOverview) view).setProfileList(result.getEntries());
                }

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log(caught.getMessage(), caught);
                }
            });
        }
    }

    @Override
    public void removeSailorProfile(UUID uuid) {
        sharedSailorProfilePresenter.getDataProvider().removeSailorProfile(uuid, new AsyncCallback<SailorProfileDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfileDTO result) {
                sharedSailorProfilePresenter.getDataProvider()
                        .loadSailorProfiles(new AsyncCallback<SailorProfilesDTO>() {

                            @Override
                            public void onSuccess(SailorProfilesDTO result) {
                                ((SailorProfileOverview) view).setProfileList(result.getEntries());
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log(caught.getMessage(), caught);
                            }
                        });
            }
        });
    }

    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }

    @Override
    public FlagImageResolver getFlagImageResolver() {
        return this.flagImageResolver;
    }

}
