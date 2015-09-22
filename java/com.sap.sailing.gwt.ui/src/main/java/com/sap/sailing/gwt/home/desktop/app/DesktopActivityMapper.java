package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringActivityProxy;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactPlace;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.desktop.places.searchresult.SearchResultActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.desktop.places.start.StartActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.start.StartPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationPlaceUpdater;

public class DesktopActivityMapper implements ActivityMapper {
    private final DesktopClientFactory clientFactory;
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    public DesktopActivityMapper(DesktopClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place rawPlace) {
        Place place = placeUpdater.getRealPlace(rawPlace);
        if (SwitchingEntryPoint.isMobile() //
                && !SwitchingEntryPoint.viewIsLockedToDesktop()
                && SwitchingEntryPoint.hasMobileVersion(place)) {
            SwitchingEntryPoint.reloadApp();
            return null;
        }
        if (place instanceof AboutUsPlace) {
            return new AboutUsActivityProxy((AboutUsPlace) place, clientFactory);
        } else if (place instanceof ErrorPlace) {
            return new ErrorActivityProxy((ErrorPlace) place, clientFactory);
        } else if (place instanceof ContactPlace) {
            return new ContactActivityProxy((ContactPlace) place, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            AbstractEventPlace eventPlace = (AbstractEventPlace) place;
            return new EventActivityProxy(eventPlace, clientFactory, clientFactory.getHomePlacesNavigator());
        } else if (place instanceof AbstractSeriesPlace) {
            return new com.sap.sailing.gwt.home.client.place.fakeseries.SeriesActivityProxy((AbstractSeriesPlace) place, clientFactory, clientFactory.getHomePlacesNavigator());
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
        } else if (place instanceof SearchResultPlace) {
            return new SearchResultActivityProxy((SearchResultPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
