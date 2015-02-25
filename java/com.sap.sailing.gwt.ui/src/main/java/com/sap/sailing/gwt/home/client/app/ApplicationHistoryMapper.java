package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.media.MultiRegattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.overview.MultiRegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.regattas.EventRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.leaderboard.EventLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.media.MediaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.races.EventRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
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
        EventDefaultPlace.Tokenizer.class,//
        // Multiregatta places:
        MultiRegattaMediaPlace.Tokenizer.class, //
        MultiRegattaOverviewPlace.Tokenizer.class,//
        EventRegattasPlace.Tokenizer.class,//
        // Regatta places:
        RegattaOverviewPlace.Tokenizer.class,//
        EventRacesPlace.Tokenizer.class, //
        EventLeaderboardPlace.Tokenizer.class, //
        MediaPlace.Tokenizer.class

})
public interface ApplicationHistoryMapper extends PlaceHistoryMapper {
}
