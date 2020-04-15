package com.sap.sailing.gwt.home.desktop.places.fakeseries.eventstab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class SeriesEventsPlace extends AbstractSeriesTabPlace implements HasMobileVersion {
    public SeriesEventsPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.SeriesEvents)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesEventsPlace> {

        @Override
        protected SeriesEventsPlace getRealPlace(SeriesContext context) {
            return new SeriesEventsPlace(context);
        }
    }
}
