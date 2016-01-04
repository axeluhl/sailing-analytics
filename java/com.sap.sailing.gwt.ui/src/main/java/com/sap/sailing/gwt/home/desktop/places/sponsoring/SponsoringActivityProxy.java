package com.sap.sailing.gwt.home.desktop.places.sponsoring;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SponsoringActivityProxy extends AbstractActivityProxy {

    private final SponsoringClientFactory clientFactory;
    private final SponsoringPlace place;

    public SponsoringActivityProxy(SponsoringPlace place, SponsoringClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SponsoringActivity(place, clientFactory));
            }
        });
    }
}
