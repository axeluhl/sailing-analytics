package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;

public abstract class AutoPlayPresenterBase<P extends Place> extends AbstractActivity {

    private P place;
    private AutoPlayClientFactory clientFactory;

    public AutoPlayPresenterBase(P place, AutoPlayClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    protected AutoPlayContext getSlideCtx() {
        return clientFactory.getAutoPlayCtxSignalError();
    }

    public AutoPlayClientFactory getClientFactory() {
        return clientFactory;
    }

    protected P getPlace() {
        return place;
    }
}
