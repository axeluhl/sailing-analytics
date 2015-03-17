package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class EventSeriesCompetitorAnalyticsPlace extends AbstractSeriesTabPlace {
    public EventSeriesCompetitorAnalyticsPlace(String id) {
        super(id);
    }

    public EventSeriesCompetitorAnalyticsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.EventSeriesCompetitorAnalytics)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesCompetitorAnalyticsPlace> {
        @Override
        protected EventSeriesCompetitorAnalyticsPlace getRealPlace(SeriesContext context) {
            return new EventSeriesCompetitorAnalyticsPlace(context);
        }
    }
}
