package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class Slide2PresenterImpl extends SlideBase<Slide2Place> implements Slide2View.Slide2Presenter {

    private Slide2View view;

    public Slide2PresenterImpl(Slide2Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide2View slide2ViewImpl) {
        super(place, clientFactory);
        this.view = slide2ViewImpl;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
