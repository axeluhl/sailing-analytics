package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class LeaderboardActivityProxy extends AbstractActivityProxy {

    private final LeaderboardClientFactory clientFactory;
    private final LeaderboardPlace place;

    public LeaderboardActivityProxy(LeaderboardPlace place, LeaderboardClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new LeaderboardActivity(place, clientFactory));
            }
        });
    }
}
