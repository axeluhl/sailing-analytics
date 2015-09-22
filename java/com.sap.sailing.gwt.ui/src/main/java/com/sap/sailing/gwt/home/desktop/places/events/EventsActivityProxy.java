package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventsActivityProxy extends AbstractActivityProxy {

    private final EventsClientFactory clientFactory;
    private final EventsPlace place;

    public EventsActivityProxy(EventsPlace place, EventsClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventsActivity(place, clientFactory));
            }
        });
    }
}
