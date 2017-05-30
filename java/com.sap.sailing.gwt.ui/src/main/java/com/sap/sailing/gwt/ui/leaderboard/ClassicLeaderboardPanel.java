package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;


public class ClassicLeaderboardPanel extends LeaderboardPanel {

    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);

    public ClassicLeaderboardPanel(Component<?> parent, ComponentContext<?> context, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings, boolean isEmbedded,
            RegattaAndRaceIdentifier preSelectedRace, CompetitorSelectionProvider competitorSelectionProvider,
            String leaderboardGroupName, String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, boolean showRaceDetails) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace,
                competitorSelectionProvider, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                showRaceDetails, resources, componentResources, tableResources);
    }

    public ClassicLeaderboardPanel(Component<?> parent, ComponentContext<?> context, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings, boolean isEmbedded,
            RegattaAndRaceIdentifier preSelectedRace, CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, String leaderboardGroupName, String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, boolean showRaceDetails, CompetitorFilterPanel competitorSearchTextBox,
            boolean showSelectionCheckbox, RaceTimesInfoProvider optionalRaceTimesInfoProvider,
            boolean autoExpandLastRaceColumn, boolean adjustTimerDelay, boolean autoApplyTopNFilter,
            boolean showCompetitorFilterStatus, boolean enableSyncScroller) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace,
                competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, showRaceDetails, competitorSearchTextBox, showSelectionCheckbox,
                optionalRaceTimesInfoProvider, autoExpandLastRaceColumn, adjustTimerDelay, autoApplyTopNFilter,
                showCompetitorFilterStatus, enableSyncScroller, resources, componentResources, tableResources);
    }

    public ClassicLeaderboardPanel(Component<?> parent, ComponentContext<?> context, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings,
            CompetitorSelectionProvider competitorSelectionProvider, String leaderboardName,
            ErrorReporter errorReporter, StringMessages stringMessages, boolean showRaceDetails) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, competitorSelectionProvider,
                leaderboardName, errorReporter, stringMessages, showRaceDetails, resources, componentResources,
                tableResources);
    }

    @Override
    public void renderNationalityFlag(ImageResource nationalityFlagImageResource, SafeHtmlBuilder sb) {
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        sb.append(renderer.render(nationalityFlagImageResource));             
    }

    @Override
    protected void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb,CompetitorDTO competitor) {
        sb.appendHtmlConstant("<img src=\"" + flagImageURL + "\" width=\"18px\" height=\"12px\" title=\"" + competitor.getName() + "\"/>");        
    }

}
