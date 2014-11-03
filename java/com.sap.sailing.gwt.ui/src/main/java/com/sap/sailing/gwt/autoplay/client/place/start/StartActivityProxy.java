package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class StartActivityProxy extends AbstractActivityProxy {

    private final StartClientFactory clientFactory;
    private final StartPlace place;

    public StartActivityProxy(StartPlace place, StartClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new StartActivity(place, clientFactory));
            }
        });
    }
}
