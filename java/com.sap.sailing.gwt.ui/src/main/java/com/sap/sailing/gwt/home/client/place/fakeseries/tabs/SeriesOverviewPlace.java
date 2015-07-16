package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class SeriesOverviewPlace extends AbstractSeriesTabPlace implements HasMobileVersion {
    public SeriesOverviewPlace(String id) {
        super(id);
    }

    public SeriesOverviewPlace(SeriesContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.SeriesOverview)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesOverviewPlace> {

        @Override
        protected SeriesOverviewPlace getRealPlace(SeriesContext context) {
            return new SeriesOverviewPlace(context);
        }
    }
}
