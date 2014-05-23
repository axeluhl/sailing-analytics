package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.app.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.client.app.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.app.event.EventActivityProxy;
import com.sap.sailing.gwt.home.client.app.event.EventPlace;
import com.sap.sailing.gwt.home.client.app.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.client.app.events.EventsPlace;
import com.sap.sailing.gwt.home.client.app.start.StartActivityProxy;
import com.sap.sailing.gwt.home.client.app.start.StartPlace;

public class ApplicationActivityMapper implements ActivityMapper {
    private final ApplicationClientFactory clientFactory;

    public ApplicationActivityMapper(ApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof AboutUsPlace) {
            return new AboutUsActivityProxy((AboutUsPlace) place, clientFactory);
        } else if (place instanceof ContactPlace) {
            return new ContactActivityProxy((ContactPlace) place, clientFactory);
        } else if (place instanceof EventPlace) {
            return new EventActivityProxy((EventPlace) place, clientFactory);
        } else if (place instanceof EventsPlace) {
            return new EventsActivityProxy((EventsPlace) place, clientFactory);
        } else if (place instanceof StartPlace) {
            return new StartActivityProxy((StartPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
