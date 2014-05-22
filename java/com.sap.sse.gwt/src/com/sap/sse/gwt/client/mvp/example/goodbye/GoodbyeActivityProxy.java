package com.sap.sse.gwt.client.mvp.example.goodbye;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.gwt.client.mvp.example.AppClientFactory;

public class GoodbyeActivityProxy extends AbstractActivityProxy {

    private final AppClientFactory clientFactory;
    private final GoodbyePlace place;

    public GoodbyeActivityProxy(GoodbyePlace place, AppClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new GoodbyeActivity(place, clientFactory));
            }
        });
    }
}
