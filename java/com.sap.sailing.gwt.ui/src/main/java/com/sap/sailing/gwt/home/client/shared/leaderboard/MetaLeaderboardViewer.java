package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class MetaLeaderboardViewer extends AbstractRegattaAnalyticsManager {    
    private MultiLeaderboardPanel multiLeaderboardPanel;
    
    public MetaLeaderboardViewer(SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer, ErrorReporter errorReporter, UserAgentDetails userAgent) {
        super(sailingService, asyncActionsExecutor, timer, errorReporter, userAgent);
        
//        new LeaderboardPanel(sailingService, asyncActionsExecutor,
//                leaderboardSettings, true, preselectedRace, competitorSelectionModel, timer,
//                leaderboardGroupName, metaLeaderboardName, errorReporter, StringMessages.INSTANCE, userAgent,
//                showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, 
//                /* adjustTimerDelay */ true, /*autoApplyTopNFilter*/false, false)
        
        final Label overallStandingsLabel = new Label(StringMessages.INSTANCE.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");
    
    }
    
    public MultiLeaderboardPanel createMetaLeaderboardPanel(LeaderboardSettings leaderboardSettings,
            String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {
        if(multiLeaderboardPanel == null) {
            multiLeaderboardPanel = new MultiLeaderboardPanel(sailingService, metaLeaderboardName, asyncActionsExecutor, timer,
                    preselectedLeaderboardName, preselectedRace, errorReporter, StringMessages.INSTANCE,
                    userAgent, showRaceDetails, autoExpandLastRaceColumn);
        }
        return multiLeaderboardPanel;
    }

    public MultiLeaderboardPanel getMultiLeaderboardPanel() {
        return multiLeaderboardPanel;
    }
}
