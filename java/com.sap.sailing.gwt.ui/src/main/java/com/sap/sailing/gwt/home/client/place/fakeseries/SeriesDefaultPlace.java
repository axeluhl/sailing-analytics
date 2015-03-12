package com.sap.sailing.gwt.home.client.place.fakeseries;


public class SeriesDefaultPlace extends AbstractSeriesPlace {

    public SeriesDefaultPlace(SeriesContext ctx) {
        super(ctx);
    }

    public SeriesDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesDefaultPlace> {
        @Override
        protected SeriesDefaultPlace getRealPlace(SeriesContext context) {
            return new SeriesDefaultPlace(context);
        }
    }
}
