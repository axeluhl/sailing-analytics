package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.mobile.places.series.OverallLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;

public interface SeriesMiniOverallLeaderboardView {

    public interface Presenter extends OverallLeaderboardNavigationProvider {
        DispatchSystem getDispatch();

        SeriesContext getCtx();
    }

    Widget asWidget();
}
