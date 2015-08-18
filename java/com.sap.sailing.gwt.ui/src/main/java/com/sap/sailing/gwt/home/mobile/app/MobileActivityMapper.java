package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.races.RacesActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.SeriesActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.start.StartActivityProxy;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationPlaceUpdater;

public class MobileActivityMapper implements ActivityMapper {
    private final MobileApplicationClientFactory clientFactory;
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    public MobileActivityMapper(MobileApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place rawPlace) {
        Place place = placeUpdater.getRealPlace(rawPlace);
        if (!SwitchingEntryPoint.hasMobileVersion(place)) {
            GWT.log("Place has no mobile view: " + place.getClass().getName());
            SwitchingEntryPoint.reloadApp();
            return null;
        }
        if (place instanceof StartPlace) {
            return new StartActivityProxy((StartPlace) place, clientFactory);
        } else if (place instanceof EventsPlace) {
            return new EventsActivityProxy((EventsPlace) place, clientFactory);
        } else if (place instanceof RegattaRacesPlace) {
            return new RacesActivityProxy((RegattaRacesPlace) place, clientFactory);
        } else if (place instanceof MiniLeaderboardPlace) {
            return new MiniLeaderboardActivityProxy((MiniLeaderboardPlace) place, clientFactory);
        } else if (place instanceof LatestNewsPlace) {
            return new LatestNewsActivityProxy((LatestNewsPlace) place, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            return new EventActivityProxy((AbstractEventPlace) place, clientFactory);
        } else if (place instanceof SeriesMiniOverallLeaderboardPlace) {
            return new SeriesMiniOverallLeaderboardActivityProxy((SeriesMiniOverallLeaderboardPlace) place, clientFactory);
        } else if (place instanceof AbstractSeriesPlace) {
            return new SeriesActivityProxy((AbstractSeriesPlace) place, clientFactory);
        } else if (place instanceof SolutionsPlace) {
            return new SolutionsActivityProxy((SolutionsPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
