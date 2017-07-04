package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.resources.LeaderboardTableResourcesSixty;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel.LeaderBoardStyle;
import com.sap.sse.gwt.client.shared.components.ComponentResources;

public class SixtyInchLeaderBoardStyle implements LeaderBoardStyle {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResourcesSixty tableResources = GWT
            .create(LeaderboardTableResourcesSixty.class);
    private static final FlagRendererTemplate FLAG_RENDERER = GWT.create(FlagRendererTemplate.class);
    
    private LeaderboardDTO laterInit;
    private LeaderboardPanel leaderBoardPanel;
    private boolean showRaceColumns;
    private boolean ready;
    
    public SixtyInchLeaderBoardStyle(boolean showRaceColumns) {
        this.showRaceColumns = showRaceColumns;
    }

    @Override
    public void renderNationalityFlag(ImageResource nationalityFlagImageResource, SafeHtmlBuilder sb) {
        sb.append(FLAG_RENDERER.flag(nationalityFlagImageResource.getSafeUri(), 60, 40, ""));
    }

    @Override
    public void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb, CompetitorDTO competitor) {
        sb.append(FLAG_RENDERER.flag(UriUtils.fromString(flagImageURL), 60, 40, competitor.getName()));
    }

    interface FlagRendererTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<img src='{0}' border='0' width='{1}' height='{2}' title='{3}'>")
        SafeHtml flag(SafeUri imageUri, int width, int height, String title);
    }

    @Override
    public LeaderboardResources getResources() {
        return resources;
    }

    @Override
    public ComponentResources getComponentresources() {
        return componentResources;
    }

    @Override
    public LeaderboardTableResources getTableresources() {
        return tableResources;
    }

    @Override
    public void processStyleForTotalNetPointsColumn(String textColor, SafeStylesBuilder ssb) {
        ssb.trustedColor(textColor);
    }
    
    @Override
    public String determineBoatColorDivStyle(String competitorColor) {
        return "border-right: 4px solid " + competitorColor + ";";
    }

    @Override
    public void processStyleForRaceColumnWithoutReasonForMaxPoints(boolean isDiscarded, SafeStylesBuilder ssb) {
        if (isDiscarded) {
            ssb.opacity(0.5d);
        } else {
            // we don't bold...
            ssb.appendTrustedString("font-weight:300;");
        }
    }

    @Override
    public void afterConstructorHook(FlowPanel contentPanel, LeaderboardPanel leaderboardPanel) {
        this.leaderBoardPanel = leaderboardPanel;
        leaderboardPanel.getLeaderboardTable().getElement().getStyle().setMarginTop(0, Unit.PX);
        Widget toolbarPanel = leaderboardPanel.createToolbarPanel();
        contentPanel.add(toolbarPanel);
        leaderboardPanel.playPause.setVisible(false);

        leaderboardPanel.updateToolbar(laterInit);
        ready = true;
    }

    @Override
    public void afterLeaderboardUpdate(LeaderboardDTO leaderboard) {
        if(ready){
            leaderBoardPanel.updateToolbar(leaderboard);
        }
    }

    @Override
    public boolean preUpdateToolbarHook(LeaderboardDTO leaderboard) {
        return ready;
    }

    @Override
    public boolean hasRaceColumns() {
        return showRaceColumns;
    }
}
