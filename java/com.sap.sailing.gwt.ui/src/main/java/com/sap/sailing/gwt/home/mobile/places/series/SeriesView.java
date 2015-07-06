package com.sap.sailing.gwt.home.mobile.places.series;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;

public interface SeriesView {

    Widget asWidget();

    public interface Presenter {
        SeriesContext getCtx();
        DispatchSystem getDispatch();
        PlaceNavigation<?> getOverallLeaderboardNavigation();
        PlaceNavigation<?> getMiniOverallLeaderboardNavigation();
    }
}

