package com.sap.sailing.gwt.home.shared.places.fakeseries;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;


public class SeriesDefaultPlace extends AbstractSeriesPlace implements HasMobileVersion {

    public SeriesDefaultPlace(SeriesContext ctx) {
        super(ctx);
    }

    @Prefix(PlaceTokenPrefixes.SeriesDefault)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesDefaultPlace> {
        @Override
        protected SeriesDefaultPlace getRealPlace(SeriesContext context) {
            return new SeriesDefaultPlace(context);
        }
    }
}
