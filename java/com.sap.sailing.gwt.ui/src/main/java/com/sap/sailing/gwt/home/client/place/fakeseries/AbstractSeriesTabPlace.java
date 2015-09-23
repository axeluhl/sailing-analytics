package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;


public abstract class AbstractSeriesTabPlace extends AbstractSeriesPlace {

    public AbstractSeriesTabPlace(SeriesContext ctx) {
        super(ctx);
    }

    public AbstractSeriesTabPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }
}
