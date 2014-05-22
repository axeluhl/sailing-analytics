package com.sap.sse.gwt.client.mvp.example.hello;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.gwt.client.mvp.example.ClientFactory;

public class HelloActivityProxy extends AbstractActivityProxy {

    private final ClientFactory clientFactory;
    private final HelloPlace place;

    public HelloActivityProxy(HelloPlace place, ClientFactory clientFactory) {
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
