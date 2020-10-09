package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.desktop.app.places.EventsActivity;
import com.sap.sailing.gwt.ui.adminconsole.desktop.app.places.EventsPlace;

public class AdminConsoleDesktopActivityMapper implements ActivityMapper {

    private final AdminConsoleDesktopClientFactoryImpl clientFactory;

    public AdminConsoleDesktopActivityMapper(AdminConsoleDesktopClientFactoryImpl clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {

        if (place instanceof EventsPlace) {
            return new EventsActivity((EventsPlace) place, clientFactory);
        }
        return null;
    }
}
