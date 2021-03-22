package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class DashboardActivityProxy extends AbstractActivityProxy {

    private final ManagementConsoleClientFactory clientFactory;
    private final DashboardPlace place;

    public DashboardActivityProxy(ManagementConsoleClientFactory clientFactory, DashboardPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new DashboardActivity(clientFactory, place));
            }
        });
    }

}
