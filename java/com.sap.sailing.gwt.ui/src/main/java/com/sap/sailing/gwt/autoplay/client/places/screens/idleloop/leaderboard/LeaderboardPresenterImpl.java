package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.MultiRaceLeaderboardWithHeaderPerspective;

public class LeaderboardPresenterImpl extends AutoPlayPresenterConfigured<LeaderboardPlace>
        implements LeaderboardView.Presenter {

    private LeaderboardView view;


    public LeaderboardPresenterImpl(LeaderboardPlace place, AutoPlayClientFactory clientFactory,
            LeaderboardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        MultiRaceLeaderboardWithHeaderPerspective leaderboardWithHeaderPerspective = getPlace().getLeaderboardPerspective();

        view.startingWith(this, panel, leaderboardWithHeaderPerspective);
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}
