package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class RegattaOverviewActivityProxy extends AbstractActivityProxy {

    private final ManagementConsoleClientFactory clientFactory;
    private final RegattaOverviewPlace place;

    public RegattaOverviewActivityProxy(ManagementConsoleClientFactory clientFactory, RegattaOverviewPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new RegattaOverviewActivity(clientFactory, place));
            }
        });
    }

}
