package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.MultiRaceLeaderboardWithZoomingPerspective;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class LeaderboardViewImpl extends ResizeComposite implements LeaderboardView {
    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    protected ResizableFlowPanel holderUi;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, LeaderboardViewImpl> {
    }

    public LeaderboardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void onStop() {
    }


    @Override
    public void startingWith(Presenter p, AcceptsOneWidget panel,
            MultiRaceLeaderboardWithZoomingPerspective leaderboardWithHeaderPerspective) {
        this.asWidget().ensureDebugId("LeaderboardView");
        holderUi.add(leaderboardWithHeaderPerspective);
        leaderboardWithHeaderPerspective.getElement().getStyle().setHeight(100, Unit.PCT);
        panel.setWidget(this);
    }

}
