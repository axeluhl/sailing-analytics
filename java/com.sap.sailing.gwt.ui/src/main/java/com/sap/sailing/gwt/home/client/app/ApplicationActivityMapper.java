package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventActivityProxy;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsActivityProxy;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultActivityProxy;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
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
            // return new EventActivityProxy((EventPlace) place, clientFactory);
            // rerouting old place to new place to make bookmarked URLs to work with the new interface.
            return getActivity(getRealEventPlace((EventPlace) place));
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
        } else if (place instanceof RegattaPlace) {
//            return new RegattaActivityProxy((RegattaPlace) place, clientFactory);
            // rerouting old place to new place to make bookmarked URLs to work with the new interface.
            return getActivity(getRealRegattaPlace((RegattaPlace) place));
        } else if (place instanceof SeriesPlace) {
//            return new SeriesActivityProxy((SeriesPlace) place, clientFactory);
            // rerouting old place to new place to make bookmarked URLs to work with the new interface.
            return getActivity(getRealSeriesPlace((SeriesPlace) place));
        } else if (place instanceof SearchResultPlace) {
            return new SearchResultActivityProxy((SearchResultPlace) place, clientFactory);
        } else {
            return null;
        }
    }

    private Place getRealRegattaPlace(RegattaPlace place) {
        String eventId = place.getEventUuidAsString();
        if(eventId == null || eventId.isEmpty()) {
            return new EventsPlace();
        }
        EventContext eventContext = new EventContext().withId(eventId);
        String regattaId = place.getLeaderboardIdAsNameString();
        boolean hasRegattaId = (regattaId != null && !regattaId.isEmpty());
        if(hasRegattaId) {
            switch (place.getNavigationTab()) {
            case CompetitorAnalytics:
                return new RegattaCompetitorAnalyticsPlace(eventContext);
            case Leaderboard:
                return new RegattaLeaderboardPlace(eventContext);
            }
        }
        return new EventDefaultPlace(eventContext);
    }

    @SuppressWarnings("incomplete-switch")
    private Place getRealEventPlace(EventPlace place) {
        String eventId = place.getEventUuidAsString();
        if(eventId == null || eventId.isEmpty()) {
            return new EventsPlace();
        }
        EventContext eventContext = new EventContext().withId(eventId);
        String regattaId = place.getLeaderboardIdAsNameString();
        boolean hasRegattaId = (regattaId != null && !regattaId.isEmpty());
        if(hasRegattaId) {
            eventContext.withRegattaId(regattaId);
            
            switch (place.getNavigationTab()) {
            // TODO some places aren't implemented yet 
            case Media:
                return new RegattaMediaPlace(eventContext);
            case Overview:
                // Overview not implemented yet -> using regatta list
            case Regatta:
                return new RegattaRacesPlace(eventContext);
            }
        } else {
            switch (place.getNavigationTab()) {
            case Media:
                return new MultiregattaMediaPlace(eventContext);
            case Overview:
                // Overview not implemented yet -> using race list
            case Schedule:
                // Schedule not implemented yet -> using race list
            case Regattas:
                return new MultiregattaRegattasPlace(eventContext);
            }
        }
        
        return new EventDefaultPlace(eventContext);
    }
    
    private Place getRealSeriesPlace(SeriesPlace place) {
        String seriesId = place.getEventUuidAsString();
        SeriesContext context = new SeriesContext().withId(seriesId);
        
        // TODO evaluate additional parameters
        
        switch (place.getNavigationTab()) {
            case OverallLeaderboard:
                return new EventSeriesLeaderboardPlace(context);
            case RegattaLeaderboards:
                // TODO regatta Leaderboards aren't ported over yet
                return new EventSeriesLeaderboardPlace(context);
            case CompetitorAnalytics:
                return new EventSeriesCompetitorAnalyticsPlace(context);
        }
        return new SeriesDefaultPlace(context);
    }
}
