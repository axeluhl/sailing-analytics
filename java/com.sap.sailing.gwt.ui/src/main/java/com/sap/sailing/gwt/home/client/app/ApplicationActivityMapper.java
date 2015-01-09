package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventActivityProxy;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaActivityProxy;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultActivityProxy;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesActivityProxy;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringActivityProxy;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartActivityProxy;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewActivityProxy;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;

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
        } else if (place instanceof SponsoringPlace) {
            return new SponsoringActivityProxy((SponsoringPlace) place, clientFactory);
        } else if (place instanceof SolutionsPlace) {
            return new SolutionsActivityProxy((SolutionsPlace) place, clientFactory);
        } else if (place instanceof WhatsNewPlace) {
            return new WhatsNewActivityProxy((WhatsNewPlace) place, clientFactory);
        } else if (place instanceof RegattaPlace) {
            return new RegattaActivityProxy((RegattaPlace) place, clientFactory);
        } else if (place instanceof SeriesPlace) {
            return new SeriesActivityProxy((SeriesPlace) place, clientFactory);
        } else if (place instanceof SearchResultPlace) {
            return new SearchResultActivityProxy((SearchResultPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
