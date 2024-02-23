package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class CreateEventSeriesActivityProxy extends AbstractActivityProxy {

    private final ManagementConsoleClientFactory clientFactory;
    private final CreateEventSeriesPlace place;

    public CreateEventSeriesActivityProxy(ManagementConsoleClientFactory clientFactory, CreateEventSeriesPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new CreateEventSeriesActivity(clientFactory, place));
            }
        });
    }

}
