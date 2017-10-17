package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

public class MoreLoginInformationActivity implements Activity {
    private DesktopClientFactory clientFactory;

    public MoreLoginInformationActivity(Place place, DesktopClientFactory clientFactory) {
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
        panel.setWidget(new MoreLoginInformation(new Runnable() {
            @Override
            public void run() {
                clientFactory.getEventBus().fireEvent(new AuthenticationRequestEvent(true));
            }
        }));
    }

}
