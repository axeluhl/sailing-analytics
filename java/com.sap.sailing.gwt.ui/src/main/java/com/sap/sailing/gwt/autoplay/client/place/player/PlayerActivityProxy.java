package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class PlayerActivityProxy extends AbstractActivityProxy {

    private final PlayerClientFactory clientFactory;
    private final PlayerPlace place;

    public PlayerActivityProxy(PlayerPlace place, PlayerClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new PlayerActivity(place, clientFactory));
            }
        });
    }
}
