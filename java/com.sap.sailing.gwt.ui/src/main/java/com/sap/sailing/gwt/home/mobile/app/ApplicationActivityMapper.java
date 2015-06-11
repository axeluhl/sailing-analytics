package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.start.StartActivityProxy;


public class ApplicationActivityMapper implements ActivityMapper {
    private final MobileApplicationClientFactory clientFactory;

    public ApplicationActivityMapper(MobileApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof StartPlace) {
            return new StartActivityProxy((StartPlace) place, clientFactory);
        } else if (place instanceof EventPlace) {
            EventPlace eventPlace = (EventPlace) place;
            return new EventActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof EventsPlace) {
            return new EventsActivityProxy((EventsPlace) place, clientFactory);
        } else {
            return null;
        }
    }

}
