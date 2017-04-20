package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.SixtyInchContext;

public abstract class PresenterBase<P extends Place> extends AbstractActivity {

    private P place;
    private AutoPlayClientFactorySixtyInch clientFactory;

    public PresenterBase(P place, AutoPlayClientFactorySixtyInch clientFactory) {
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
