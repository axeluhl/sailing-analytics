package com.sap.sailing.gwt.home.mobile.places.events;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventsActivityProxy extends AbstractActivityProxy {

    private final ApplicationContext clientFactory;
    private final EventsPlace currentPlace;

    public EventsActivityProxy(EventsPlace place, ApplicationContext clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventsActivity(currentPlace, clientFactory));
            }
        });
    }
}
