package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;

public abstract class SixtyInchPresenterBase<P extends Place> extends AbstractActivity {

    private P place;
    private AutoPlayClientFactorySixtyInch clientFactory;

    public SixtyInchPresenterBase(P place, AutoPlayClientFactorySixtyInch clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    protected SixtyInchContext getSlideCtx() {
        return clientFactory.getSlideCtx();
    }

    public AutoPlayClientFactorySixtyInch getClientFactory() {
        return clientFactory;
    }

    protected P getPlace() {
        return place;
    }
}
