package com.sap.sailing.gwt.home.desktop.places.fakeseries.leaderboardstab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class EventSeriesLeaderboardsPlace extends AbstractSeriesTabPlace {
    public EventSeriesLeaderboardsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.EventSeriesLeaderboards)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesLeaderboardsPlace> {
        @Override
        protected EventSeriesLeaderboardsPlace getRealPlace(SeriesContext context) {
            return new EventSeriesLeaderboardsPlace(context);
        }
    }
}
