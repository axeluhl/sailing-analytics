package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class AbstractSeriesTabPlace extends AbstractSeriesPlace {

    public AbstractSeriesTabPlace(SeriesContext ctx) {
        super(ctx);
    }

    public AbstractSeriesTabPlace(String eventUuidAsString, String regattaId) {
        super(eventUuidAsString);
        getCtx().withRegattaId(regattaId);
    }

    public static abstract class Tokenizer<PLACE extends AbstractSeriesTabPlace> implements PlaceTokenizer<PLACE> {
        @Override
        public String getToken(PLACE place) {
            return place.getEventUuidAsString() + ";" + place.getRegattaId();
        }

        @Override
        public PLACE getPlace(String token) {
            String[] elements = token.split(";");
            return getRealPlace(elements[0], elements[1]);
        }
        
        protected abstract PLACE getRealPlace(String eventId, String regattaId);
    }
}
