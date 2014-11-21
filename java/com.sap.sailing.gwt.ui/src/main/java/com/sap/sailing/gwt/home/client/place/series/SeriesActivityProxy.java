package com.sap.sailing.gwt.home.client.place.series;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy {

    private final SeriesClientFactory clientFactory;
    private final SeriesPlace place;

    public SeriesActivityProxy(SeriesPlace place, SeriesClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SeriesActivity(place, clientFactory));
            }
        });
    }
}
