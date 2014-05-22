package com.sap.sse.gwt.client.mvp.example.hello;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.gwt.client.mvp.example.AppClientFactory;

public class HelloActivityProxy extends AbstractActivityProxy {

    private final AppClientFactory clientFactory;
    private final HelloPlace place;

    public HelloActivityProxy(HelloPlace place, AppClientFactory clientFactory) {
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
