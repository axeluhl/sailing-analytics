package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.autoplay.client.resources.LeaderboardTableResourcesSixty;
import com.sap.sailing.gwt.ui.client.shared.controls.FlushableSortedCellTableWithStylableHeaders;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel.LeaderBoardStyle;
import com.sap.sse.gwt.client.shared.components.ComponentResources;

public class SixtyInchLeaderboardStyle implements LeaderBoardStyle {
    private static final LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static final ComponentResources componentResources = GWT.create(ComponentResources.class);
    private static final LeaderboardTableResourcesSixty tableResources = GWT
            .create(LeaderboardTableResourcesSixty.class);
    private static final Template FLAG_RENDERER = GWT.create(Template.class);
    
    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div title='{3}' style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:{1}px;height:{2}px;background-image:url({0})'></div>")
        SafeHtml imageWithTitle(String imageUri,int width, int height,String title);
    }

    
    private boolean showRaceColumns;
    
    public SixtyInchLeaderboardStyle(boolean showRaceColumns) {
        this.showRaceColumns = showRaceColumns;
    }

    @Override
    public void renderNationalityFlag(ImageResource nationalityFlagImageResource, SafeHtmlBuilder sb) {
        sb.append(FLAG_RENDERER.imageWithTitle(nationalityFlagImageResource.getSafeUri().asString(), 60, 40, ""));
    }

    @Override
    public void renderFlagImage(String flagImageURL, SafeHtmlBuilder sb, CompetitorDTO competitor) {
        sb.append(FLAG_RENDERER.imageWithTitle(flagImageURL, 60, 40, competitor.getName()));
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
    public void afterConstructorHook(LeaderboardPanel<?> leaderboardPanel) {
        leaderboardPanel.getLeaderboardTable().getElement().getStyle().setMarginTop(0, Unit.PX);
        leaderboardPanel.getElement().getStyle().setWidth(100, Unit.PCT);
        leaderboardPanel.getElement().getStyle().setHeight(100, Unit.PCT);
        if (leaderboardPanel.playPause != null) {
            leaderboardPanel.playPause.setVisible(false);
        }
        Widget toolBar = leaderboardPanel.getToolbarWidget(); 
        if(toolBar != null) {
            Style style = toolBar.getElement().getStyle();
            style.setPosition(Position.ABSOLUTE);
            style.setLeft(0, Unit.PX);
            style.setRight(0, Unit.PX);
            style.setBottom(0, Unit.PX);
        }
    }

    @Override
    public boolean hasRaceColumns() {
        return showRaceColumns;
    }

    @Override
    public void hookLeaderBoardAttachment(FlowPanel contentPanel,
            FlushableSortedCellTableWithStylableHeaders<LeaderboardRowDTO> leaderboardTable) {
        SimplePanel wrapper = new SimplePanel(leaderboardTable);
        contentPanel.add(wrapper);
        contentPanel.getElement().getStyle().setWidth(100, Unit.PCT);
        contentPanel.getElement().getStyle().setHeight(100, Unit.PCT);
        contentPanel.getElement().getStyle().setPosition(Position.RELATIVE);
        Style style = wrapper.getElement().getStyle();
        style.setOverflow(Overflow.HIDDEN);
        style.setLeft(0, Unit.PX);;
        style.setRight(0, Unit.PX);
        style.setTop(0, Unit.PX);
        style.setBottom(70, Unit.PX);
        style.setPosition(Position.ABSOLUTE);
    }
}
