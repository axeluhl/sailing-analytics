package com.sap.sailing.gwt.home.desktop.places.qrcode;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class QRCodeActivityProxy extends AbstractActivityProxy {

    private final QRCodeClientFactory clientFactory;
    private final QRCodePlace place;

    public QRCodeActivityProxy(QRCodePlace place, QRCodeClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new QRCodeActivity(place, clientFactory));
            }
        });
    }
}
