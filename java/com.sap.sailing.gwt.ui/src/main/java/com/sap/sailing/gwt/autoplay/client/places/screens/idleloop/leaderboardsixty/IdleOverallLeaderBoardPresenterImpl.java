package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;

public class IdleOverallLeaderBoardPresenterImpl extends AutoPlayPresenterConfigured<IdleOverallLeaderBoardPlace>
        implements IdleOverallLeaderBoardView.Slide7Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private IdleOverallLeaderBoardView view;

    public IdleOverallLeaderBoardPresenterImpl(IdleOverallLeaderBoardPlace place, AutoPlayClientFactory clientFactory,
            IdleOverallLeaderBoardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        try {
            view.startingWith(this, panel, getPlace().getLeaderboardPanel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}
