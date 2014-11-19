package com.sap.sailing.gwt.home.client.place.series;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.seriesanalytics.EventSeriesAnalytics;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class TabletAndDesktopSeriesView extends Composite implements SeriesAnalyticsView {
    private static TabletAndDesktopSeriesViewUiBinder uiBinder = GWT.create(TabletAndDesktopSeriesViewUiBinder.class);

    interface TabletAndDesktopSeriesViewUiBinder extends UiBinder<Widget, TabletAndDesktopSeriesView> {
    }

    @UiField(provided=true) EventSeriesAnalytics seriesAnalytics;
    
    public TabletAndDesktopSeriesView(EventDTO event, String leaderboardName, SeriesNavigationTabs navigationTab, Timer timerForClientServerOffset, HomePlacesNavigator placeNavigator) {
        seriesAnalytics = new EventSeriesAnalytics(event, leaderboardName, navigationTab, timerForClientServerOffset, placeNavigator);
        
        initWidget(uiBinder.createAndBindUi(this));

        seriesAnalytics.setVisible(false);
    }
    
    public void createSeriesAnalyticsViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        seriesAnalytics.createSeriesAnalyticsViewer(sailingService, asyncActionsExecutor, timer, leaderboardSettings, preselectedLeaderboardName,
                preselectedRace, leaderboardGroupName, metaLeaderboardName, errorReporter, userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
        seriesAnalytics.setVisible(true);
    }
}
