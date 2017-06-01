package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class NoRaceColumnsSixtyInchLeaderboard extends SixtyInchLeaderBoard {

    public NoRaceColumnsSixtyInchLeaderboard(SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings, boolean isEmbedded,
            RegattaAndRaceIdentifier preSelectedRace, CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, String leaderboardGroupName, String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, boolean showRaceDetails,
            CompetitorFilterPanel competitorSearchTextBox, boolean showSelectionCheckbox,
            RaceTimesInfoProvider optionalRaceTimesInfoProvider, boolean autoExpandLastRaceColumn,
            boolean adjustTimerDelay, boolean autoApplyTopNFilter, boolean showCompetitorFilterStatus,
            boolean enableSyncScroller) {
        super(sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace, competitorSelectionProvider, timer,
                leaderboardGroupName, leaderboardName, errorReporter, stringMessages, userAgent, showRaceDetails,
                competitorSearchTextBox, showSelectionCheckbox, optionalRaceTimesInfoProvider, autoExpandLastRaceColumn,
                adjustTimerDelay, autoApplyTopNFilter, showCompetitorFilterStatus, enableSyncScroller);
    }

    protected void createMissingAndAdjustExistingRaceColumns(LeaderboardDTO leaderboard) {
         
    }
}
