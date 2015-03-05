package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class AbstractSeriesTabPlace extends AbstractSeriesPlace {

    public AbstractSeriesTabPlace(SeriesContext ctx) {
        super(ctx);
    }

    public AbstractSeriesTabPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    public static abstract class Tokenizer<PLACE extends AbstractSeriesTabPlace> implements PlaceTokenizer<PLACE> {
        @Override
        public String getToken(PLACE place) {
            return place.getSeriesUuidAsString();
        }

        @Override
        public PLACE getPlace(String token) {
            return getRealPlace(token);
        }
        
        protected abstract PLACE getRealPlace(String seriesId);
    }
}
