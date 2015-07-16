package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesMiniOverallLeaderboardActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final SeriesMiniOverallLeaderboardPlace currentPlace;

    public SeriesMiniOverallLeaderboardActivityProxy(SeriesMiniOverallLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SeriesMiniOverallLeaderboardActivity(currentPlace, clientFactory));
            }
        });
    }
}
