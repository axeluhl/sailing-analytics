package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;

public abstract class ClassicPresenterBase<P extends Place> extends AbstractActivity {

    private P place;
    private AutoPlayClientFactoryClassic clientFactory;

    public ClassicPresenterBase(P place, AutoPlayClientFactoryClassic clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    protected ClassicContext getSlideCtx() {
        return clientFactory.getSlideCtx();
    }

    public AutoPlayClientFactoryClassic getClientFactory() {
        return clientFactory;
    }

    protected P getPlace() {
        return place;
    }
}
