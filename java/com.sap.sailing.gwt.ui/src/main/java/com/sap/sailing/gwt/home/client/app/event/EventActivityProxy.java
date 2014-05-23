package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final EventClientFactory clientFactory;
    private final EventPlace place;

    public EventActivityProxy(EventPlace place, EventClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventActivity(place, clientFactory));
            }
        });
    }
}
