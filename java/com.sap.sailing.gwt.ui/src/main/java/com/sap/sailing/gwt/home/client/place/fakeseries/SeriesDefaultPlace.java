package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.PlaceTokenizer;

public class SeriesDefaultPlace extends AbstractSeriesPlace {

    public SeriesDefaultPlace(SeriesContext ctx) {
        super(ctx);
    }

    public SeriesDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    public static class Tokenizer implements PlaceTokenizer<SeriesDefaultPlace> {
        @Override
        public String getToken(SeriesDefaultPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public SeriesDefaultPlace getPlace(String token) {
            return new SeriesDefaultPlace(token);
        }
    }
}
