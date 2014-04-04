package com.sap.sailing.gwt.home.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

public class MyPlaceManager extends PlaceManagerImpl {
    private final PlaceRequest defaultPlaceRequest;

    @Inject
    public MyPlaceManager(final EventBus eventBus, final TokenFormatter tokenFormatter,
            @DefaultPlace String defaultNameToken) {
        super(eventBus, tokenFormatter);

        this.defaultPlaceRequest = new PlaceRequest.Builder().nameToken(defaultNameToken).build();
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(defaultPlaceRequest);
    }
}