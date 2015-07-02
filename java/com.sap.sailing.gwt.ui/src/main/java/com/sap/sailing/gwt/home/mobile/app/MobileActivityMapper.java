package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardPlace;
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
            GWT.log("Place has no mobile view: " + place.getClass().getName());
            SwitchingEntryPoint.reloadApp();
            return null;
        }
        if (place instanceof StartPlace) {
            return new EventsActivityProxy(new EventsPlace(), clientFactory);
        } else if (place instanceof EventsPlace) {
            return new EventsActivityProxy((EventsPlace) place, clientFactory);
        } else if (place instanceof MiniLeaderboardPlace) {
            return new MiniLeaderboardActivityProxy((MiniLeaderboardPlace) place, clientFactory);
        } else if (place instanceof LatestNewsPlace) {
            return new LatestNewsActivityProxy((LatestNewsPlace) place, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            return new EventActivityProxy((AbstractEventPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
