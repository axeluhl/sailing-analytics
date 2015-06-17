package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardActivity;
import com.sap.sailing.gwt.home.mobile.places.notmobile.NotMobileActivityProxy;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;


public class MobileActivityMapper implements ActivityMapper {
    private final MobileApplicationClientFactory clientFactory;

    private Place lastVisitedPlace = new StartPlace();

    public MobileActivityMapper(MobileApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (!(place instanceof HasMobileVersion)) {
            return new NotMobileActivityProxy(lastVisitedPlace, place, clientFactory);
        }
        if (place instanceof StartPlace) {
            // going to events place
            // lastVisitedPlace = place;
            // return new StartActivityProxy((StartPlace) place, clientFactory);
            lastVisitedPlace = place;
            return new EventsActivityProxy(new EventsPlace(), clientFactory);
        } else if (place instanceof EventsPlace) {
            lastVisitedPlace = place;
            EventsPlace eventPlace = (EventsPlace) place;
            return new EventsActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof RegattaLeaderboardPlace) {
            lastVisitedPlace = place;
            RegattaLeaderboardPlace eventPlace = (RegattaLeaderboardPlace) place;
            return new MiniLeaderboardActivity(eventPlace, clientFactory);
        } else if (place instanceof LatestNewsPlace) {
            lastVisitedPlace = place;
            LatestNewsPlace eventPlace = (LatestNewsPlace) place;
            return new LatestNewsActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            lastVisitedPlace = place;
            return new EventActivityProxy((AbstractEventPlace) place, clientFactory);
        } else {
            return null;
        }
    }

}
