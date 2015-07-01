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
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardActivityProxy;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;


public class MobileActivityMapper implements ActivityMapper {
    private final MobileApplicationClientFactory clientFactory;

    public MobileActivityMapper(MobileApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (!(place instanceof HasMobileVersion)) {

            SwitchingEntryPoint.switchToDesktop();
            return null;
        }
        if (place instanceof StartPlace) {
            return new EventsActivityProxy(new EventsPlace(), clientFactory);
        } else if (place instanceof EventsPlace) {
            EventsPlace eventPlace = (EventsPlace) place;
            return new EventsActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof RegattaLeaderboardPlace) {
            RegattaLeaderboardPlace eventPlace = (RegattaLeaderboardPlace) place;
            return new MiniLeaderboardActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof LatestNewsPlace) {
            LatestNewsPlace eventPlace = (LatestNewsPlace) place;
            return new LatestNewsActivityProxy(eventPlace, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            return new EventActivityProxy((AbstractEventPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
