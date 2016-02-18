package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
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
                super.onSuccess(new UserProfileDetailsActivity(currentPlace, clientFactory));
            }
        });
    }
}
