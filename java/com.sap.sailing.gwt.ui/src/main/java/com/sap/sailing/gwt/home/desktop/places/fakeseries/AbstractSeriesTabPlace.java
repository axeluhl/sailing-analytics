package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.UUID;

import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;


public abstract class AbstractSeriesTabPlace extends AbstractSeriesPlace {

    public AbstractSeriesTabPlace(SeriesContext ctx) {
        super(ctx);
    }

    public AbstractSeriesTabPlace(String leaderboardGroupName) {
        super(UUID.fromString(leaderboardGroupName));
    }
}
