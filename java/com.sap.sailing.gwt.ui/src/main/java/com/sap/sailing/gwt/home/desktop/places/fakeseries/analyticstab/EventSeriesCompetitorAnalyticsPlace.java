package com.sap.sailing.gwt.home.desktop.places.fakeseries.analyticstab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class EventSeriesCompetitorAnalyticsPlace extends AbstractSeriesTabPlace {
    public EventSeriesCompetitorAnalyticsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.EventSeriesCompetitorAnalytics)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesCompetitorAnalyticsPlace> {
        @Override
        protected EventSeriesCompetitorAnalyticsPlace getRealPlace(SeriesContext context) {
            return new EventSeriesCompetitorAnalyticsPlace(context);
        }
    }
}
