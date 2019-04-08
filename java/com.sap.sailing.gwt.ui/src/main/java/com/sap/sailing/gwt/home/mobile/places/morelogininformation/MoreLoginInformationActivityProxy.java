package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.MoreLoginInformationPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class MoreLoginInformationActivityProxy extends AbstractActivityProxy {

    private final MoreLoginInformationPlace currentPlace;
    private final MobileApplicationClientFactory clientFactory;

    public MoreLoginInformationActivityProxy(MoreLoginInformationPlace currentPlace,
            MobileApplicationClientFactory clientFactory) {
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
