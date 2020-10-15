package com.sap.sailing.gwt.ui.pwa;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.pwa.desktop.places.DesktopEventsActivity;
import com.sap.sailing.gwt.ui.pwa.desktop.places.DesktopEventsPlace;

public class AdminConsoleDesktopActivityMapper implements ActivityMapper {

    private final AdminConsoleDesktopClientFactoryImpl clientFactory;

    public AdminConsoleDesktopActivityMapper(AdminConsoleDesktopClientFactoryImpl clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {

        if (place instanceof DesktopEventsPlace) {
            return new DesktopEventsActivity((DesktopEventsPlace) place, clientFactory);
        }
        return null;
    }
}
