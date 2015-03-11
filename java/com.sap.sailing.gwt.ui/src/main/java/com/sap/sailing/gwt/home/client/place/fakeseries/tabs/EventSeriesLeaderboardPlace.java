package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class EventSeriesLeaderboardPlace extends AbstractSeriesTabPlace {
    public EventSeriesLeaderboardPlace(String id) {
        super(id);
    }
    
    public EventSeriesLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesLeaderboardPlace> {
        @Override
        protected EventSeriesLeaderboardPlace getRealPlace(SeriesContext context) {
            return new EventSeriesLeaderboardPlace(context);
        }
    }
}
