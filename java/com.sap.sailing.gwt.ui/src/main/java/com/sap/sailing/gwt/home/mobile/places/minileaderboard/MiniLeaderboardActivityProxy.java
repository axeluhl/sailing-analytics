package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class MiniLeaderboardActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final RegattaLeaderboardPlace currentPlace;

    public MiniLeaderboardActivityProxy(RegattaLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new MiniLeaderboardActivity(currentPlace, clientFactory));
            }
        });
    }
}
