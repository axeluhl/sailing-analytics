package com.sap.sailing.gwt.home.client.place.event.legacy;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface SeriesClientFactory extends SailingClientFactory, ClientFactoryWithDispatch, ErrorAndBusyClientFactory {

    // SeriesAnalyticsView createSeriesAnalyticsView(EventDTO event, String leaderboardName, SeriesNavigationTabs
    // navigationTab, Timer timerForClientServerOffset);
}
