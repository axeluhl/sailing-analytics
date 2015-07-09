package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;


public class SeriesDefaultPlace extends AbstractSeriesPlace implements HasMobileVersion {

    public SeriesDefaultPlace(SeriesContext ctx) {
        super(ctx);
    }

    public SeriesDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    @Prefix(EventPrefixes.SeriesDefault)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesDefaultPlace> {
        @Override
        protected SeriesDefaultPlace getRealPlace(SeriesContext context) {
            return new SeriesDefaultPlace(context);
        }
    }
}
