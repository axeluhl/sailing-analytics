package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final ApplicationContext clientFactory;
    private final EventPlace currentPlace;

    public EventActivityProxy(EventPlace place, ApplicationContext clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventActivity(currentPlace, clientFactory));
            }
        });
    }
}
