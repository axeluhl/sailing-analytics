package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.leaderboard.RegattaAnalytics;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class TabletAndDesktopLeaderboardView extends Composite implements LeaderboardView {
    private static LeaderboardPageViewUiBinder uiBinder = GWT.create(LeaderboardPageViewUiBinder.class);

    interface LeaderboardPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopLeaderboardView> {
    }

    @UiField(provided=true) RegattaAnalytics regattaAnalytics;
    
    public TabletAndDesktopLeaderboardView(EventDTO event, String leaderboardName, Timer timerForClientServerOffset, PlaceNavigator placeNavigator) {
        regattaAnalytics = new RegattaAnalytics(event, leaderboardName, timerForClientServerOffset, placeNavigator);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void createLeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        regattaAnalytics.createLeaderboardViewer(sailingService, asyncActionsExecutor, timer, leaderboardSettings, preselectedRace,
                leaderboardGroupName, leaderboardName, errorReporter, userAgent, showRaceDetails, autoExpandLastRaceColumn, showOverallLeaderboard);
    }

    public void createMetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        regattaAnalytics.createMetaLeaderboardViewer(sailingService, asyncActionsExecutor, timer, leaderboardSettings, preselectedLeaderboardName,
                preselectedRace, leaderboardGroupName, metaLeaderboardName, errorReporter, userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
    }
}
