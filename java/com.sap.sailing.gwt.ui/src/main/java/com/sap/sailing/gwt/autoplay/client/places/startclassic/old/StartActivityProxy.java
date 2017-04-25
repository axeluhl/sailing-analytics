package com.sap.sailing.gwt.autoplay.client.places.startclassic.old;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class StartActivityProxy extends AbstractActivityProxy {

    private final StartClientFactory clientFactory;

    public StartActivityProxy(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new StartActivity(clientFactory));
            }
        });
    }
}
