package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SailingAnalyticsActivityProxy extends AbstractActivityProxy {

    private final SailingAnalyticsClientFactory clientFactory;
    private final SailingAnalyticsPlace place;

    public SailingAnalyticsActivityProxy(SailingAnalyticsPlace place, SailingAnalyticsClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SailingAnalyticsActivity(place, clientFactory));
            }
        });
    }
}
