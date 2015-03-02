package com.sap.sailing.gwt.home.client.place.series;

import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface SeriesClientFactory extends SailingClientFactory {

    SeriesAnalyticsView createSeriesAnalyticsView(EventDTO event, String leaderboardName, SeriesNavigationTabs navigationTab, Timer timerForClientServerOffset);
}
