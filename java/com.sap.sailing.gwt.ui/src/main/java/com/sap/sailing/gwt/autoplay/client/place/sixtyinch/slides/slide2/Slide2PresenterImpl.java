package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;

public class Slide2PresenterImpl extends SlideBase<Slide2Place> implements Slide2View.Slide2Presenter {

    private Slide2View view;

    public Slide2PresenterImpl(Slide2Place place, SlideContext slideCtx, Slide2View slide2ViewImpl) {
        super(place, slideCtx);
        this.view = slide2ViewImpl;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getLeaderboardName()));
        view.startingWith(this, panel);
    }
}
