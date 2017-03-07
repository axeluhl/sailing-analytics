package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;

public class Slide2PresenterImpl extends SlideBase<Slide2Place> implements Slide2View.Slide1Presenter {

    private Slide2View view;

    public Slide2PresenterImpl(Slide2Place place, SlideContext slideCtx) {
        super(place, slideCtx);

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.startingWith(this);
    }
}
