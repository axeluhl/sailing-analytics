package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class ImprintActivityProxy extends AbstractActivityProxy {

    private final ImprintPlace place;

    public ImprintActivityProxy(ImprintPlace place) {
        this.place = place;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new ImprintActivity(place));
            }
        });
    }
}
