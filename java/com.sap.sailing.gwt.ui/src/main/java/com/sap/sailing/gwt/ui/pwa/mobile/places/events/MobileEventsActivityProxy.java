package com.sap.sailing.gwt.ui.pwa.mobile.places.events;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.pwa.PwaClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class MobileEventsActivityProxy extends AbstractActivityProxy {

    private final PwaClientFactory clientFactory;
    private final MobileEventsPlace place;

    public MobileEventsActivityProxy(MobileEventsPlace place, PwaClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new MobileEventsActivity(place, clientFactory));
            }
        });
    }
}
