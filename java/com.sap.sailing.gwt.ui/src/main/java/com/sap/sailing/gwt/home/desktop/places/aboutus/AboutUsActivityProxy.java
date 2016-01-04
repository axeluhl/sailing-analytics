package com.sap.sailing.gwt.home.desktop.places.aboutus;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class AboutUsActivityProxy extends AbstractActivityProxy {

    private final AboutUsClientFactory clientFactory;
    private final AboutUsPlace place;

    public AboutUsActivityProxy(AboutUsPlace place, AboutUsClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new AboutUsActivity(place, clientFactory));
            }
        });
    }
}
