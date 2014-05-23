package com.sap.sse.gwt.client.mvp.example.goodbye;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class GoodbyeActivityProxy extends AbstractActivityProxy {

    private final GoodbyeViewFactory clientFactory;
    private final GoodbyePlace place;

    public GoodbyeActivityProxy(GoodbyePlace place, GoodbyeViewFactory clientFactory) {
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
