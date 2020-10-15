package com.sap.sailing.gwt.ui.pwa;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.pwa.mobile.places.events.MobileEventsActivityProxy;
import com.sap.sailing.gwt.ui.pwa.mobile.places.events.MobileEventsPlace;

public class AdminConsoleMobileActivityMapper implements ActivityMapper {

    private final PwaClientFactory clientFactory;

    public AdminConsoleMobileActivityMapper(PwaClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof MobileEventsPlace) {
            return new MobileEventsActivityProxy((MobileEventsPlace) place, clientFactory);
        }
        return null;
    }
}
