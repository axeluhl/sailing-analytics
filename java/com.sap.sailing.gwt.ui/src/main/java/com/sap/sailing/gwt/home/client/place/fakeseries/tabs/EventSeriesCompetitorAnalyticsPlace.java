package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

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

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<EventSeriesCompetitorAnalyticsPlace> {
        @Override
        protected EventSeriesCompetitorAnalyticsPlace getRealPlace(SeriesContext context) {
            return new EventSeriesCompetitorAnalyticsPlace(context);
        }
    }
}
