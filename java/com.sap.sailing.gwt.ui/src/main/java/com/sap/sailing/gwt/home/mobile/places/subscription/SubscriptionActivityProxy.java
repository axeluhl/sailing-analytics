package com.sap.sailing.gwt.home.mobile.places.subscription;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SubscriptionActivityProxy extends AbstractActivityProxy {

    private final SubscriptionPlace place;
    private final MobileApplicationClientFactory clientFactory;

    public SubscriptionActivityProxy(final SubscriptionPlace place,
            final MobileApplicationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SubscriptionActivity(place, clientFactory));
            }
        });
    }
}
