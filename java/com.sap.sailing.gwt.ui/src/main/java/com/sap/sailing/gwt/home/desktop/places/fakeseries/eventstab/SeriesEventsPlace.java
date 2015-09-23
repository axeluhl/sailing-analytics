package com.sap.sailing.gwt.home.desktop.places.fakeseries.eventstab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class SeriesEventsPlace extends AbstractSeriesTabPlace {
    public SeriesEventsPlace(String id) {
        super(id);
    }

    public SeriesEventsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.SeriesEvents)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesEventsPlace> {

        @Override
        protected SeriesEventsPlace getRealPlace(SeriesContext context) {
            return new SeriesEventsPlace(context);
        }
    }
}
