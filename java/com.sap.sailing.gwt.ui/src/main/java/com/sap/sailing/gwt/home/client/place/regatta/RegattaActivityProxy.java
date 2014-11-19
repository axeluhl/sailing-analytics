package com.sap.sailing.gwt.home.client.place.regatta;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class RegattaActivityProxy extends AbstractActivityProxy {

    private final RegattaClientFactory clientFactory;
    private final RegattaPlace place;

    public RegattaActivityProxy(RegattaPlace place, RegattaClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new RegattaActivity(place, clientFactory));
            }
        });
    }
}
