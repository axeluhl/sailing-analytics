package com.sap.sailing.gwt.home.desktop.places.solutions;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SolutionsActivityProxy extends AbstractActivityProxy {

    private final SolutionsClientFactory clientFactory;
    private final SolutionsPlace place;

    public SolutionsActivityProxy(SolutionsPlace place, SolutionsClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SolutionsActivity(place, clientFactory));
            }
        });
    }
}
