package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassic;
import com.sap.sailing.gwt.autoplay.client.app.classic.ClassicConfiguredPresenter;

public class LeaderboardPresenterImpl extends ClassicConfiguredPresenter<LeaderboardPlace>
        implements LeaderboardView.Presenter {

    private LeaderboardView view;


    public LeaderboardPresenterImpl(LeaderboardPlace place, AutoPlayClientFactoryClassic clientFactory,
            LeaderboardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        view.startingWith(this, panel, getPlace().getLeaderboardPerspective());
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}
