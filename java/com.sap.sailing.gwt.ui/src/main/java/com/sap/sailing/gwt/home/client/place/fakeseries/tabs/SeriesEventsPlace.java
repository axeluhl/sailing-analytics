package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class SeriesEventsPlace extends AbstractSeriesTabPlace {
    public SeriesEventsPlace(String id) {
        super(id);
    }

    public SeriesEventsPlace(SeriesContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesEventsPlace> {

        @Override
        protected SeriesEventsPlace getRealPlace(SeriesContext context) {
            return new SeriesEventsPlace(context);
        }
    }
}
