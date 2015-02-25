package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.leaderboard.EventLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.overview.multiregatta.MultiRegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.races.EventRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;

@WithTokenizers({ AboutUsPlace.Tokenizer.class, ContactPlace.Tokenizer.class, EventPlace.Tokenizer.class, EventsPlace.Tokenizer.class, 
    SolutionsPlace.Tokenizer.class, WhatsNewPlace.Tokenizer.class, SponsoringPlace.Tokenizer.class, SearchResultPlace.Tokenizer.class, StartPlace.Tokenizer.class,
        RegattaPlace.Tokenizer.class, SeriesPlace.Tokenizer.class,
        // Event tab places...
        com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace.Tokenizer.class,//
        MultiRegattaOverviewPlace.Tokenizer.class,//
        MultiRegattaOverviewPlace.Tokenizer.class,//
        EventRacesPlace.Tokenizer.class,
        EventLeaderboardPlace.Tokenizer.class

})
public interface ApplicationHistoryMapper extends PlaceHistoryMapper {
}
