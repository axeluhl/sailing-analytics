package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;

@WithTokenizers({ //
EventPlace.Tokenizer.class,//
        EventsPlace.Tokenizer.class,//
        EventDefaultPlace.Tokenizer.class,//
        LatestNewsPlace.Tokenizer.class,//
        RegattaLeaderboardPlace.Tokenizer.class, //
        StartPlace.Tokenizer.class
})
public interface MobileHistoryMapper extends PlaceHistoryMapper {
}
