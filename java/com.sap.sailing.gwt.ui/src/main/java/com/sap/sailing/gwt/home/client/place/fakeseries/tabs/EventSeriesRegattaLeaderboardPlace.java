package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class EventSeriesRegattaLeaderboardPlace extends AbstractSeriesTabPlace {
    public EventSeriesRegattaLeaderboardPlace(String id) {
        super(id);
    }
    
    public EventSeriesRegattaLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesRegattaLeaderboardPlace> {
        @Override
        protected EventSeriesRegattaLeaderboardPlace getRealPlace(SeriesContext context) {
            return new EventSeriesRegattaLeaderboardPlace(context);
        }
    }
}
