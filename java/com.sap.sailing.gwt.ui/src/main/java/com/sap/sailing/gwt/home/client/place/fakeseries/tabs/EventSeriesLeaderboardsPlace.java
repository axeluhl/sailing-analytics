package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class EventSeriesLeaderboardsPlace extends AbstractSeriesTabPlace {
    public EventSeriesLeaderboardsPlace(String id) {
        super(id);
    }
    
    public EventSeriesLeaderboardsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.EventSeriesLeaderboards)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesLeaderboardsPlace> {
        @Override
        protected EventSeriesLeaderboardsPlace getRealPlace(SeriesContext context) {
            return new EventSeriesLeaderboardsPlace(context);
        }
    }
}
