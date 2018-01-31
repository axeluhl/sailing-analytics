package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel.LeaderBoardStyle;
import com.sap.sse.gwt.client.shared.components.ComponentResources;

public class ClassicLeaderboardStyle implements LeaderBoardStyle {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);
    
    private static final int RENDERED_FLAG_WIDTH = 18;
    private static final int RENDERED_FLAG_HEIGHT = 12;

    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:{1}px;height:{2}px;background-image:url({0})'></div>")
        SafeHtml image(String imageUri,int width, int height);
        @SafeHtmlTemplates.Template("<div title='{3}' style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:{1}px;height:{2}px;background-image:url({0})'></div>")
        SafeHtml imageWithTitle(String imageUri,int width, int height,String title);
    }

    private static final Template TEMPLATE = GWT.create(Template.class);

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
        sb.append(TEMPLATE.image(nationalityFlagImageResource.getSafeUri().asString(),RENDERED_FLAG_WIDTH,RENDERED_FLAG_HEIGHT));
    }

    @Override
    public void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb, CompetitorDTO competitor) {
        sb.append(TEMPLATE.imageWithTitle(flagImageURL,RENDERED_FLAG_WIDTH,RENDERED_FLAG_HEIGHT,competitor.getName()));
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
    public void afterConstructorHook(FlowPanel contentPanel, LeaderboardPanel<?> leaderboardPanel) {
    }

    @Override
    public void afterLeaderboardUpdate(LeaderboardDTO leaderboard) {
    }

    @Override
    public boolean preUpdateToolbarHook(LeaderboardDTO leaderboard) {
        return true;
    }

    @Override
    public boolean hasRaceColumns() {
        return true;
    }

}
