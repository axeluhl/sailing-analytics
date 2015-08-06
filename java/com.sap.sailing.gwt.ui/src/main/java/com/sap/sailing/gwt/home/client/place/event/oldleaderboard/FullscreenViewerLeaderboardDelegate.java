package com.sap.sailing.gwt.home.client.place.event.oldleaderboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard.OldLeaderboardDelegate;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Color;

public class FullscreenViewerLeaderboardDelegate extends FullscreenContainer<LeaderboardPanel> implements OldLeaderboardDelegate {
    
    private final Image autoRefreshControl = new Image("images/home/reload.png");
    private final Image settingsControl = new Image("images/home/settings.png");
    
    private final Label lastScoringUpdateTime = new Label();
    private final Label lastScoringUpdateText = new Label();
    private final Label lastScoringComment = new Label();
    private final Label scoringScheme = new Label();
    private final Label hasLiveRace = new Label();
    
    public FullscreenViewerLeaderboardDelegate() {
        showLogo();
        showBorder();
        setHeaderWidget(createPanel(lastScoringComment, hasLiveRace, scoringScheme));
        addToolbarInfo(createPanel(lastScoringUpdateText, lastScoringUpdateTime));
        addToolbarAction(autoRefreshControl);
        addToolbarAction(settingsControl);
    }
    
    @Override
    protected void onShow() {
        Element leaderboardPanel = getContentWidget().getElement();
        leaderboardPanel.getStyle().setBackgroundColor(Color.WHITE.getAsHtml());
        leaderboardPanel.getFirstChildElement().getStyle().setMarginTop(-10, Unit.PX);
        leaderboardPanel.setTabIndex(0);
        leaderboardPanel.focus();
    }
    
    @Override
    protected void onClose() {
        Element leaderboardPanel = getContentWidget().getElement();
        leaderboardPanel.getStyle().clearBackgroundColor();
        leaderboardPanel.getFirstChildElement().getStyle().clearMarginTop();
        leaderboardPanel.blur();
        leaderboardPanel.removeAttribute("tabIndex");
    }
    
    private Widget createPanel(Widget... contentWidgets) {
        FlowPanel panel = new FlowPanel();
        for (Widget widget : contentWidgets) panel.add(widget);
        return panel;
    }
    
    @Override
    public void setLeaderboardPanel(LeaderboardPanel leaderboardPanel) {
        showContent(leaderboardPanel);
    }
    
    @Override
    public Widget getAutoRefreshControl() {
        return autoRefreshControl;
    }

    @Override
    public Widget getSettingsControl() {
        return settingsControl;
    }
    
    @Override
    public Element getLastScoringUpdateTimeElement() {
        return lastScoringUpdateTime.getElement();
    }
    
    @Override
    public Element getLastScoringUpdateTextElement() {
        return lastScoringUpdateText.getElement();
    }
    
    @Override
    public Element getLastScoringCommentElement() {
        return lastScoringComment.getElement();
    }
    
    @Override
    public Element getHasLiveRaceElement() {
        return hasLiveRace.getElement();
    }
    
    @Override
    public Element getScoringSchemeElement() {
        return scoringScheme.getElement();
    }
}