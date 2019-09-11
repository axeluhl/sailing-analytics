package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileTabView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.UserProfileDetailsPlace;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class UserProfileDetailsTabView extends Composite implements UserProfileTabView<UserProfileDetailsPlace> {

    private UserProfileDetailsView.Presenter currentPresenter;

    private UserProfileDetailsView view;

    public UserProfileDetailsTabView() {
    }

    @Override
    public Class<UserProfileDetailsPlace> getPlaceClassForActivation() {
        return UserProfileDetailsPlace.class;
    }

    @Override
    public void start(UserProfileDetailsPlace myPlace, AcceptsOneWidget contentArea) {
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
    public UserProfileDetailsPlace placeToFire() {
        return new UserProfileDetailsPlace();
    }

    @Override
    public void setPresenter(UserProfileView.Presenter currentPresenter) {
        view = new UserProfileDetailsViewImpl();
        this.currentPresenter = new UserProfileDetailsPresenter(view, currentPresenter);
    }
}