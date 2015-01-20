package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class WhatsNewActivityProxy extends AbstractActivityProxy {

    private final WhatsNewClientFactory clientFactory;
    private final WhatsNewPlace place;

    public WhatsNewActivityProxy(WhatsNewPlace place, WhatsNewClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new WhatsNewActivity(place, clientFactory));
            }
        });
    }
}
