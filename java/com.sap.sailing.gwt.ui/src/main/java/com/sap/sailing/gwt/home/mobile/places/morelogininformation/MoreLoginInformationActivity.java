package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;

public class MoreLoginInformationActivity implements Activity {
    private MobileApplicationClientFactory clientFactory;

    public MoreLoginInformationActivity(Place place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new MoreLoginInformationMobile(new Runnable() {
            @Override
            public void run() {
                clientFactory.getNavigator().getSignInNavigation(true).goToPlace();
            }
        }));
    }

}
