package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewImpl;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.utils.LeaderBoardScaleHelper;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class LiveRaceWithRacemapAndLeaderBoardViewImpl extends ResizeComposite implements LiveRaceWithRacemapAndLeaderBoardView {
    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    private Timer resizer;

    private RaceMap rawRaceMap;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, LiveRaceWithRacemapAndLeaderBoardViewImpl> {
    }

    public LiveRaceWithRacemapAndLeaderBoardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void showErrorNoLive(LiveRaceWithRacemapAndLeaderBoardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error) {
        panel.setWidget(new Label("Could not load RaceMap: " + error.getMessage()));
    }

    @Override
    public void onCompetitorSelect(CompetitorDTO marked) {
        rawRaceMap.addedToSelection(marked);
    }

    @Override
    public void scrollLeaderBoardToTop() {
        leaderBoardHolder.getElement().setScrollTop(0);
    }

    @Override
    public void onStop() {
        resizer.cancel();
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap,
            SixtyInchLeaderBoard leaderboardPanel) {
        panel.setWidget(this);
        rawRaceMap = raceMap;
        racemap.add(raceMap);
        resizeMapOnceInitially(raceMap);

        leaderBoardHolder.add(leaderboardPanel);
        resizer = new Timer() {

            @Override
            public void run() {
                LeaderBoardScaleHelper.scaleContentWidget(AutoPlayMainViewImpl.SAP_HEADER_IN_PX,
                        leaderboardPanel);
            }
        };
        resizer.scheduleRepeating(100);
    }


    private void resizeMapOnceInitially(RaceMap raceMap2) {
        new Timer() {
            @Override
            public void run() {
                GWT.log("reinit fallback");
                raceMap2.redraw();
                racemap.onResize();
                if(racemap.isAttached()){
                    this.schedule(50);
                }
            }
        }.schedule(50);
    }

}
