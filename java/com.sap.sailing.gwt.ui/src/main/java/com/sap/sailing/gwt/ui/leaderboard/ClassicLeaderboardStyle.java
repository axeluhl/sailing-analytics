package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.FlagImageRenderer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel.LeaderBoardStyle;
import com.sap.sse.gwt.client.celltable.FlushableSortedCellTableWithStylableHeaders;
import com.sap.sse.gwt.client.shared.components.ComponentResources;

public class ClassicLeaderboardStyle implements LeaderBoardStyle {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);
    
    public LeaderboardResources getResources() {
        return resources;
    }

    public ComponentResources getComponentresources() {
        return componentResources;
    }

    public LeaderboardTableResources getTableresources() {
        return tableResources;
    }

    @Override
    public void renderNationalityFlag(ImageResource nationalityFlagImageResource, SafeHtmlBuilder sb) {
        sb.append(FlagImageRenderer.image(nationalityFlagImageResource.getSafeUri().asString()));
    }

    @Override
    public void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb, CompetitorDTO competitor) {
        sb.append(FlagImageRenderer.imageWithTitle(flagImageURL, competitor.getName()));
    }

    @Override
    public void processStyleForTotalNetPointsColumn(String textColor, SafeStylesBuilder ssb) {
        ssb.fontWeight(FontWeight.BOLD);
        ssb.trustedColor(textColor);
    }

    @Override
    public String determineBoatColorDivStyle(String competitorColor) {
        return "border-bottom: 2px solid " + competitorColor + ";";
    }

    @Override
    public void processStyleForRaceColumnWithoutReasonForMaxPoints(boolean isDiscarded, SafeStylesBuilder ssb) {
        if (isDiscarded) {
            ssb.opacity(0.5d);
        } else {
            ssb.fontWeight(FontWeight.BOLD);
        }
    }

    @Override
    public void afterConstructorHook(LeaderboardPanel<?> leaderboardPanel) {
    }

    @Override
    public boolean hasRaceColumns() {
        return true;
    }

    @Override
    public void hookLeaderBoardAttachment(FlowPanel contentPanel,
            FlushableSortedCellTableWithStylableHeaders<LeaderboardRowDTO> leaderboardTable) {
        contentPanel.add(leaderboardTable);
    }

}
