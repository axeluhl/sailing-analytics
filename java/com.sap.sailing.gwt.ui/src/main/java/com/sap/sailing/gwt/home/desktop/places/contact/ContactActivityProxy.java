package com.sap.sailing.gwt.home.desktop.places.contact;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class ContactActivityProxy extends AbstractActivityProxy {

    private final ContactClientFactory clientFactory;
    private final ContactPlace place;

    public ContactActivityProxy(ContactPlace place, ContactClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new ContactActivity(place, clientFactory));
            }
        });
    }
}
