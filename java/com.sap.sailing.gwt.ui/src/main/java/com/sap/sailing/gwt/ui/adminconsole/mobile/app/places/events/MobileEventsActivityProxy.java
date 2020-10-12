package com.sap.sailing.gwt.ui.adminconsole.mobile.app.places.events;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class MobileEventsActivityProxy extends AbstractActivityProxy {

    private final AdminConsoleClientFactory clientFactory;
    private final MobileEventsPlace place;

    public MobileEventsActivityProxy(MobileEventsPlace place, AdminConsoleClientFactory clientFactory) {
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
