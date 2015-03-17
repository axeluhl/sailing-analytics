package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event2.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class EventSeriesOverallLeaderboardPlace extends AbstractSeriesTabPlace {
    public EventSeriesOverallLeaderboardPlace(String id) {
        super(id);
    }
    
    public EventSeriesOverallLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.EventSeriesOverallLeaderboard)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesOverallLeaderboardPlace> {
        @Override
        protected EventSeriesOverallLeaderboardPlace getRealPlace(SeriesContext context) {
            return new EventSeriesOverallLeaderboardPlace(context);
        }
    }
}
