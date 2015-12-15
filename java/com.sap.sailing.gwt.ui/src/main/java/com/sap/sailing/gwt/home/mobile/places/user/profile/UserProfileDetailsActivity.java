package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

public class UserProfileDetailsActivity extends AbstractActivity implements UserProfilDetailsView.Presenter {
    
    public UserProfileDetailsActivity(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(new UserProfileDetailsViewImpl(this));
    }
}
