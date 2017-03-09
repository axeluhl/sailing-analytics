package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;

public abstract class SlideBase<P extends Place> extends AbstractActivity implements Slide<P> {
    private P place;
    private AutoPlayClientFactorySixtyInch clientFactory;

    public SlideBase(P place, AutoPlayClientFactorySixtyInch clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    protected SlideContext getSlideCtx() {
        return clientFactory.getSlideCtx();
    }

    public AutoPlayClientFactorySixtyInch getClientFactory() {
        return clientFactory;
    }

    protected P getPlace() {
        return place;
    }
}
