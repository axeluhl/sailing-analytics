package com.sap.sse.gwt.client.mvp.example.hello;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class HelloActivityProxy extends AbstractActivityProxy {

    private final HelloViewFactory clientFactory;
    private final HelloPlace place;

    public HelloActivityProxy(HelloPlace place, HelloViewFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new HelloActivity(place, clientFactory));
            }
        });
    }
}
