package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesEventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesMediaPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
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
        // TODO not implemented: MultiregattaOverviewPlace.Tokenizer.class,
        MultiregattaRegattasPlace.Tokenizer.class,
        MultiregattaMediaPlace.Tokenizer.class,
        // Regatta places:
        // TODO not implemented: RegattaOverviewPlace.Tokenizer.class,
        RegattaRacesPlace.Tokenizer.class,
        RegattaLeaderboardPlace.Tokenizer.class,
        RegattaCompetitorAnalyticsPlace.Tokenizer.class,
        RegattaMediaPlace.Tokenizer.class,
        // Fake series places:
        SeriesDefaultPlace.Tokenizer.class,
        SeriesEventsPlace.Tokenizer.class,
        EventSeriesCompetitorAnalyticsPlace.Tokenizer.class,
        EventSeriesLeaderboardPlace.Tokenizer.class,
        SeriesMediaPlace.Tokenizer.class

})
public interface ApplicationHistoryMapper extends PlaceHistoryMapper {
}
