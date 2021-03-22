package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventSeriesOverviewActivityProxy extends AbstractActivityProxy {

    private final ManagementConsoleClientFactory clientFactory;
    private final EventSeriesOverviewPlace place;

    public EventSeriesOverviewActivityProxy(ManagementConsoleClientFactory clientFactory, EventSeriesOverviewPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventSeriesOverviewActivity(clientFactory, place));
            }
        });
    }

}
