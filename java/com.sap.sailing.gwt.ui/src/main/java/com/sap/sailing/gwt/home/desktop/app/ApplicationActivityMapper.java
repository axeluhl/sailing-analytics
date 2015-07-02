package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.error.ErrorActivityProxy;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultActivityProxy;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringActivityProxy;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartActivityProxy;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewActivityProxy;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationPlaceUpdater;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class ApplicationActivityMapper implements ActivityMapper {
    private final ApplicationClientFactory clientFactory;
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    public ApplicationActivityMapper(ApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place rawPlace) {
        Place place = placeUpdater.getRealPlace(rawPlace);
        if (place instanceof HasMobileVersion && SwitchingEntryPoint.isMobile() && !SwitchingEntryPoint.isForcedDesktop()) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    Window.Location.reload();
                }
            });
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
