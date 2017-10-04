package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class MoreLoginInformationActivity implements Activity {

    public MoreLoginInformationActivity(Place place) {
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
                GWT.debugger();
//                eventBus.fireEvent(new RegistrationRequestEvent());
            }
        }));
    }

}
