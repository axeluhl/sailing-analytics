package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesLeaderboardsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesEventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesMediaPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;

@WithTokenizers({ AboutUsPlace.Tokenizer.class, ContactPlace.Tokenizer.class, EventPlace.Tokenizer.class,
        EventsPlace.Tokenizer.class, SolutionsPlace.Tokenizer.class, WhatsNewPlace.Tokenizer.class,
        SponsoringPlace.Tokenizer.class, SearchResultPlace.Tokenizer.class, StartPlace.Tokenizer.class,
        RegattaPlace.Tokenizer.class, SeriesPlace.Tokenizer.class,
        // Event tab places...
        EventDefaultPlace.Tokenizer.class,
        // Multiregatta places:
        MultiregattaOverviewPlace.Tokenizer.class,
        MultiregattaRegattasPlace.Tokenizer.class,
        MultiregattaMediaPlace.Tokenizer.class,
        // Regatta places:
        RegattaOverviewPlace.Tokenizer.class,
        RegattaRacesPlace.Tokenizer.class,
        RegattaLeaderboardPlace.Tokenizer.class,
        RegattaCompetitorAnalyticsPlace.Tokenizer.class,
        RegattaMediaPlace.Tokenizer.class,
        // Fake series places:
        SeriesDefaultPlace.Tokenizer.class,
        SeriesEventsPlace.Tokenizer.class,
        EventSeriesCompetitorAnalyticsPlace.Tokenizer.class,
        EventSeriesOverallLeaderboardPlace.Tokenizer.class,
        EventSeriesLeaderboardsPlace.Tokenizer.class,
        SeriesMediaPlace.Tokenizer.class

})
public interface ApplicationHistoryMapper extends PlaceHistoryMapper {
}
