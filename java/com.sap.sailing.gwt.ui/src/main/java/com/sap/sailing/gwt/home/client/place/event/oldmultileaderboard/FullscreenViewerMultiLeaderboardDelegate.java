package com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard.OldMultiLeaderboard.MultiLeaderboardDelegate;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;

public class FullscreenViewerMultiLeaderboardDelegate extends FullscreenContainer implements MultiLeaderboardDelegate {
    
    private final Image autoRefreshControl = new Image("images/home/reload.png");
    private final Image settingsControl = new Image("images/home/settings.png");
    
    private final Label lastScoringUpdateTime = new Label();
    private final Label lastScoringUpdateText = new Label();
    private final Label lastScoringComment = new Label();
    private final Label scoringScheme = new Label();
    
    public FullscreenViewerMultiLeaderboardDelegate() {
        showLogo();
        showBorder();
        setHeaderWidget(createPanel(lastScoringComment, scoringScheme));
        addToolbarInfo(createPanel(lastScoringUpdateText, lastScoringUpdateTime));
        addToolbarAction(autoRefreshControl);
        addToolbarAction(settingsControl);
    }
    
    private Widget createPanel(Widget... contentWidgets) {
        FlowPanel panel = new FlowPanel();
        for (Widget widget : contentWidgets) panel.add(widget);
        return panel;
    }
    
    
    @Override
    public void setLeaderboard(MultiLeaderboardPanel multiLeaderboardPanel) {
        showContent(multiLeaderboardPanel);
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
    public Element getScoringSchemeElement() {
        return scoringScheme.getElement();
    }
}