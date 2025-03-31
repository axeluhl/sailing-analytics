package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class PasswordResetActivityProxy extends AbstractActivityProxy {
    private final PasswordResetPlace place;
    private final PasswordResetClientFactory clientFactory;

    public PasswordResetActivityProxy(PasswordResetPlace place, PasswordResetClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new PasswordResetActivity(place, clientFactory));
            }
        });
    }
}
