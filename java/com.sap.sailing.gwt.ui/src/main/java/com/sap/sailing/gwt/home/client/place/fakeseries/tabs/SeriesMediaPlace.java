package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesTabPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;

public class SeriesMediaPlace extends AbstractSeriesTabPlace {
    public SeriesMediaPlace(String id) {
        super(id);
    }

    public SeriesMediaPlace(SeriesContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesMediaPlace> {

        @Override
        protected SeriesMediaPlace getRealPlace(SeriesContext context) {
            return new SeriesMediaPlace(context);
        }
    }
}
