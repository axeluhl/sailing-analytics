package com.sap.sailing.gwt.home.mobile.places.error;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class ErrorActivityProxy extends AbstractActivityProxy {

    private final ErrorPlace place;
    private final MobileApplicationClientFactory clientFactory;

    public ErrorActivityProxy(ErrorPlace place, MobileApplicationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new ErrorActivity(place, clientFactory));
            }
        });
    }
}
