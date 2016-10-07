package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.user.profile.details.UserProfileDetailsActivity;
import com.sap.sailing.gwt.home.mobile.places.user.profile.preferences.UserProfilePreferencesActivity;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserProfilePreferencesPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class UserProfileActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final AbstractUserProfilePlace currentPlace;

    public UserProfileActivityProxy(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (currentPlace instanceof UserProfilePreferencesPlace) {
                    UserProfilePreferencesPlace userProfilePrefsPlace = (UserProfilePreferencesPlace) currentPlace;
                    super.onSuccess(new UserProfilePreferencesActivity(userProfilePrefsPlace, clientFactory));
                } else {
                    super.onSuccess(new UserProfileDetailsActivity(currentPlace, clientFactory));
                }
            }
        });
    }
}
