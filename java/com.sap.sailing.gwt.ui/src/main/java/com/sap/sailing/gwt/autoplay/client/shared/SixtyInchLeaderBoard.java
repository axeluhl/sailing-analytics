package com.sap.sailing.gwt.autoplay.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.resources.LeaderboardTableResourcesSixty;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class SixtyInchLeaderBoard extends LeaderboardPanel {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResourcesSixty tableResources = GWT
            .create(LeaderboardTableResourcesSixty.class);
    private static final FlagRendererTemplate FLAG_RENDERER = GWT.create(FlagRendererTemplate.class);

    private LeaderboardDTO laterInit;

    interface FlagRendererTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<img src='{0}' border='0' width='{1}' height='{2}' title='{3}'>")
        SafeHtml flag(SafeUri imageUri, int width, int height,String title);
    }
    
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
    protected void updateToolbar(LeaderboardDTO leaderboard) {
        // supress update call from super constructor due to special case with toolbar here
        if (playPause == null || leaderboard == null) {
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

    @Override
    public void renderNationalityFlag(ImageResource nationalityFlagImageResource, SafeHtmlBuilder sb) {
        sb.append(FLAG_RENDERER.flag(nationalityFlagImageResource.getSafeUri(), 36, 24,""));
    }
    
    @Override
    protected void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb,CompetitorDTO competitor) {
        sb.append(FLAG_RENDERER.flag(UriUtils.fromString(flagImageURL), 36, 24,competitor.getName()));
    }
}
