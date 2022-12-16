package com.sap.sailing.gwt.home.shared.places.fakeseries;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.navigation.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;


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
