package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class Slide9PresenterImpl extends SlideBase<Slide9Place> implements Slide9View.Slide9Presenter {

    private Slide9View view;

    public Slide9PresenterImpl(Slide9Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide9View slide9ViewImpl) {
        super(place, clientFactory);
        this.view = slide9ViewImpl;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
