package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SubscriptionActivityProxy extends AbstractActivityProxy {

    private final SubscriptionClientFactory clientFactory;
    private final SubscriptionPlace place;

    public SubscriptionActivityProxy(final SubscriptionPlace place, final SubscriptionClientFactory clientFactory) {
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
