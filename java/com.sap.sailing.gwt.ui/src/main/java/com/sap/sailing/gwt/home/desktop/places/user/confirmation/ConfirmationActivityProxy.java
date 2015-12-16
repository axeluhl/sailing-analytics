package com.sap.sailing.gwt.home.desktop.places.user.confirmation;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class ConfirmationActivityProxy extends AbstractActivityProxy {
    private final ConfirmationPlace place;
    private final ConfirmationClientFactory clientFactory;
    private DesktopPlacesNavigator homePlacesNavigator;

    public ConfirmationActivityProxy(ConfirmationPlace place, ConfirmationClientFactory clientFactory,
            final DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new ConfirmationActivity(place, clientFactory, homePlacesNavigator));
            }
        });
    }
}
