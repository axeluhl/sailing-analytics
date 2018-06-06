package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class IdleSixtyInchLeaderboardViewImpl extends ResizeComposite implements IdleSixtyInchLeaderboardView {
    protected static final int TOOLBAR_SIZE = 80;

    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel leaderBoardHolder;


    private MultiRaceLeaderboardPanel leaderboardPanel;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, IdleSixtyInchLeaderboardViewImpl> {
    }

    public IdleSixtyInchLeaderboardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void onStop() {
    }

    @Override
    public void scrollLeaderBoardToTop() {
        leaderBoardHolder.getElement().setScrollTop(0);
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, MultiRaceLeaderboardPanel leaderboardPanel) {
        panel.setWidget(this);
        this.leaderboardPanel = leaderboardPanel;
        leaderBoardHolder.add(leaderboardPanel);
    }

    @Override
    public void scrollIntoView(int selected) {
        leaderboardPanel.scrollRowIntoView(selected);
    }
}
