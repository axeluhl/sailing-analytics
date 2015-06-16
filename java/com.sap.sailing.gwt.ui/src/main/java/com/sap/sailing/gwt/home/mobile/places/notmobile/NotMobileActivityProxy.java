package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class NotMobileActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final Place goingTo;
    private final Place comingFrom;

    public NotMobileActivityProxy(Place comingFrom, Place goingTo, MobileApplicationClientFactory clientFactory) {
        this.comingFrom = comingFrom;
        this.goingTo = goingTo;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new NotMobileActivity(comingFrom, goingTo, clientFactory));
            }
        });
    }
}
