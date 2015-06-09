package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class StartActivityProxy extends AbstractActivityProxy {

    private final ApplicationContext clientFactory;
    private final StartPlace currentPlace;

    public StartActivityProxy(StartPlace place, ApplicationContext clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new StartActivity(currentPlace, clientFactory));
            }
        });
    }
}
