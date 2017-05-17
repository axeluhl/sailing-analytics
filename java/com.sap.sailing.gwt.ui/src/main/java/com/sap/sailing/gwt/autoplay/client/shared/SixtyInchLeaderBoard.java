package com.sap.sailing.gwt.autoplay.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.resources.LeaderboardTableResourcesSixty;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardResources;
import com.sap.sailing.gwt.ui.leaderboard.UnStyledLeaderboardPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class SixtyInchLeaderBoard extends UnStyledLeaderboardPanel {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResourcesSixty tableResources = GWT
            .create(LeaderboardTableResourcesSixty.class);
    private LeaderboardDTO laterInit;

    public SixtyInchLeaderBoard(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, boolean isEmbedded, RegattaAndRaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, CompetitorFilterPanel competitorSearchTextBox,
            boolean showSelectionCheckbox, RaceTimesInfoProvider optionalRaceTimesInfoProvider,
            boolean autoExpandLastRaceColumn, boolean adjustTimerDelay, boolean autoApplyTopNFilter,
            boolean showCompetitorFilterStatus, boolean enableSyncScroller) {
        super(null, null, sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace,
                competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, showRaceDetails, competitorSearchTextBox, showSelectionCheckbox,
                optionalRaceTimesInfoProvider, autoExpandLastRaceColumn, adjustTimerDelay, autoApplyTopNFilter,
                showCompetitorFilterStatus, enableSyncScroller, resources, componentResources, tableResources);

        getLeaderboardTable().getElement().getStyle().setMarginTop(0, Unit.PX);
        Widget toolbarPanel = createToolbarPanel();
        contentPanel.add(toolbarPanel);
        playPause.setVisible(false);
        
        updateToolbar(laterInit);
        laterInit = null;
    }
    
    @Override
    protected String determineBoatColorDivStyle(String competitorColor) {
        return "border-right: 4px solid " + competitorColor + ";";
    }

    @Override
    public int getFlagScale() {
        return 2;
    }
    
    @Override
    protected void updateToolbar(LeaderboardDTO leaderboard) {
        //supress update call from super constructor due to special case with toolbar here
        if(playPause == null || leaderboard == null){
            this.laterInit = leaderboard;
            return;
        }
        super.updateToolbar(leaderboard);
    }
    
    @Override
    public void updateLeaderboard(LeaderboardDTO leaderboard) {
        super.updateLeaderboard(leaderboard);
        updateToolbar(leaderboard);
    }

    protected void processStyleForRaceColumnWithoutReasonForMaxPoints(boolean isDiscarded, SafeStylesBuilder ssb) {
        if (isDiscarded) {
            ssb.opacity(0.5d);
        } else {
            // we don't bold...
            ssb.appendTrustedString("font-weight:300;");
        }
    }

    protected void processStyleForTotalNetPointsColumn(String textColor, SafeStylesBuilder ssb) {
        ssb.trustedColor(textColor);
    }
}
