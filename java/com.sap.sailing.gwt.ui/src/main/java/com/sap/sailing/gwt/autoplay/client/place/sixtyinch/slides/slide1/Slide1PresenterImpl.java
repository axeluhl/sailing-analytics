package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class Slide1PresenterImpl extends SlideBase<Slide1Place> implements Slide1View.Slide1Presenter {

    private Slide1View view;

    public Slide1PresenterImpl(Slide1Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide1View slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n 5 Races Rank", getSlideCtx().getLeaderboardName()));
        view.setTestText(getSlideCtx().getLeaderboardName());
        view.startingWith(this, panel);
    }
}
