package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.MoreLoginInformationPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class MoreLoginInformationActivityProxy extends AbstractActivityProxy {

    private final MoreLoginInformationPlace currentPlace;
    private final DesktopClientFactory clientFactory;

    public MoreLoginInformationActivityProxy(MoreLoginInformationPlace currentPlace,
            DesktopClientFactory clientFactory) {
        this.currentPlace = currentPlace;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new MoreLoginInformationActivity(currentPlace, clientFactory));
            }
        });
    }

}
