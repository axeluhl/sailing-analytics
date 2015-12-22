package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class ConfirmationActivityProxy extends AbstractActivityProxy {
    private final ConfirmationPlace place;
    private final ConfirmationClientFactory clientFactory;

    public ConfirmationActivityProxy(ConfirmationPlace place, ConfirmationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new ConfirmationActivity(place, clientFactory));
            }
        });
    }
}
