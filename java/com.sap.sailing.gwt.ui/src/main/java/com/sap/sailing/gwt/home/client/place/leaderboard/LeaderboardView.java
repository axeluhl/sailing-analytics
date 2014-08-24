package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public interface LeaderboardView {
    Widget asWidget();
    
    void createLeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RegattaAndRaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn,  boolean showOverallLeaderboard);

    void createMetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RegattaAndRaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards);
}
