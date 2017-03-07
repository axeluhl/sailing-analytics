package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;

public abstract class SlideBase<P extends Place> extends AbstractActivity implements Slide<P> {

    private SlideContext slideCtx;
    private P place;

    public SlideBase(P place, SlideContext slideCtx) {
        this.place = place;
        this.slideCtx = slideCtx;
    }

    protected SlideContext getSlideCtx() {
        return slideCtx;
    }

    protected P getPlace() {
        return place;
    }
}
