package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventOverviewActivityProxy extends AbstractActivityProxy {

    private final ManagementConsoleClientFactory clientFactory;
    private final EventOverviewPlace place;

    public EventOverviewActivityProxy(ManagementConsoleClientFactory clientFactory, EventOverviewPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventOverviewActivity(clientFactory, place));
            }
        });
    }

}
