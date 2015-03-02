package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class SeriesEventsPlace extends AbstractSeriesTabPlace {
    public SeriesEventsPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public SeriesEventsPlace(SeriesContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractSeriesTabPlace.Tokenizer<SeriesEventsPlace> {

        @Override
        protected SeriesEventsPlace getRealPlace(String eventId, String regattaId) {
            return new SeriesEventsPlace(eventId, regattaId);
        }
    }
}
